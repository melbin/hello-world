pipeline {
    options {
      buildDiscarder(logRotator(numToKeepStr: '3', artifactNumToKeepStr:'3'))
      timeout(time: 1, unit: 'HOURS')
      skipStagesAfterUnstable()
      retry(1)
      parallelsAlwaysFailFast()
    }
    agent none
    stages {
        stage('Maven Execution') {
          agent {
            docker {
              image 'maven:3-alpine' 
              args '-v /root/.m2:/root/.m2' 
            }
          }
          stages {
            stage('Maven Build') {
              environment {
                def config = readJSON file: "jenkins-env-${BRANCH_NAME}.json"
                release_prefix = "${config.RELEASE_PREFIX}"
              }
              steps {
                echo 'Building...'
                echo "RELEASE_PREFIX : ${release_prefix}"
                script {
                  withVault(configuration: [timeout: 60, vaultCredentialId: 'vault-token', vaultUrl: 'http://104.131.1.178:31321'], vaultSecrets: [[engineVersion: 2, path: 'secret/melbin/hello-world', secretValues: [[vaultKey: 'password'], [vaultKey: 'username'], [vaultKey: 'values-yaml']]]]) {
                    echo "testing from inside vault"
                    echo "Username = ${username}"
                    echo "values.yaml = ${values-yaml}"
                    echo "password = ${password}"
                  }
                }
                echo "Test ${MELBIN.TEST.SHOULD_FAIL}"
                sh 'mvn -B -DskipTests clean package'
                archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
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
          }
        }
        stage('Docker Deploy') {
            when {
              expression {
                currentBuild.result == null || currentBuild.result == 'SUCCESS' 
              }
            }
            agent { label 'docker' }
            environment {
              ARTIFACT_ID = readMavenPom().getArtifactId()
              PROJECT_VERSION = readMavenPom().getVersion()
            }
            steps {
                echo 'Deploying....'
                script {
                    JAR_FILE_NAME = "target/${env.ARTIFACT_ID}-${PROJECT_VERSION}.jar"
                    image = docker.build("melbin/${env.ARTIFACT_ID}:'${PROJECT_VERSION}'","-f Dockerfile --build-arg JAR_FILE='${JAR_FILE_NAME}' .")
                    image.push()
                    sh 'docker image rm -f $(docker images --format "{{.Repository}} {{.ID}}" | grep "^melbin" | cut -d " " -f2)'
                    // sh 'docker system prune --force --all --volumes'
                }
            }
        }
        stage('Kubernates Deploy') {
          agent { label 'docker' }
          environment {
            ARTIFACT_ID = readMavenPom().getArtifactId()
            PROJECT_VERSION = readMavenPom().getVersion()
            KUBECONFIG = "/tmp/configs/kubeconfig"
          }
          steps {
            sh "echo 'Deploying to kubernates'"
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