pipeline {

    // agent any
    agent { node {label 'docker'} }
    
    options { 
      buildDiscarder(logRotator(numToKeepStr: '3', artifactNumToKeepStr:'3'))
      timeout(time: 1, unit: 'HOURS')
      skipStagesAfterUnstable()
      retry(1)
      parallelsAlwaysFailFast()
    }

    environment {
      ARTIFACT_ID = readMavenPom().getArtifactId()
      PROJECT_VERSION = readMavenPom().getVersion()
      // SECRET = vault path: 'secrets', key: 'password'
      // USERNAME = vault path: 'secrets', key: 'username'
      // VALUES = vault path: 'secrets', key: 'values-yaml'
    }

    // tools {
    //   maven 'M3'
    // }
    
    stages {
        stage('Maven Execution') {
            agent {
                docker {
                    image 'maven:3-alpine'
                    args '-v /root/.m2:/root/.m2'
                }
            }
            stages {
              stage('Build') {
                steps {
                    echo 'Building...'
                    echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                    // echo "SECRET ${SECRET}"
                    // echo "USERNAME ${USERNAME}"
                    // echo "VALUES-YAML ${VALUES}"
                    sh 'mvn -B -DskipTests clean package' 
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                }
              }
            }
        }
        stage('Parallel Tests') {
            failFast true
            parallel {
              stage('JUnit') {
                steps {
                  sh 'mvn test'
                }
              }
              stage('Contracts') {
                steps {
                  echo 'Other types of parallel tests'
                  script {
                    def browsers = ['chrome', 'firefox']
                    for (int i = 0; i < browsers.size(); ++i) {
                      echo "Testing the ${browsers[i]} browser"
                    }
                  }
                }
              }
            }
        }
        stage('Docker Deploy') {
            when {
              expression {
                currentBuild.result == null || currentBuild.result == 'SUCCESS' 
              }
            }
            steps {
                echo 'Deploying....'
                script {
                    JAR_FILE_NAME = "target/${env.ARTIFACT_ID}-${PROJECT_VERSION}.jar"
                    image = docker.build("melbin/${env.ARTIFACT_ID}:'${PROJECT_VERSION}'","-f Dockerfile --build-arg JAR_FILE='${JAR_FILE_NAME}' .")
                    image.push()
                    sh 'docker system prune --force --all --volumes'
                }
            }
        }
        stage('Kubernates Deploy') {
          environment {
            KUBECONFIG = "/tmp/configs/kubeconfig"
          }
          steps {
            echo 'Deploying to kubernates'
            sh "echo 'Upgrading Helm Chart'"
            sh 'sed -i "/appVersion/c\\appVersion: ${PROJECT_VERSION}" k8s/Chart.yaml'
            sh "helm upgrade --install ${env.ARTIFACT_ID} k8s/ -f k8s/values.yaml --set container.image=melbin/${env.ARTIFACT_ID}:${PROJECT_VERSION} --wait --kubeconfig ${KUBECONFIG}"
            script {
              for (int i = 0; i < 10; i++) {
                  SERVER_STATUS = sh(returnStdout: true, script: "curl -X GET http://104.131.1.178:30000/hello-world/v1.0.0/test -H 'accept: */*' -s -o health -w '%{http_code}' --max-time 60").trim()
                  echo "HTTP-${SERVER_STATUS}"
                  if (!SERVER_STATUS.contains('200')) {
                      echo "We can't reach out the server http://104.131.1.178:30000"
                      sleep 5
                  }else{
                      break;
                  }
              }
              if (!SERVER_STATUS.contains('200')) {
                  error("We can't reach out the server http://104.131.1.178:30000")
              }
            }
          }
        }
    }
}
