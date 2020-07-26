pipeline {

    agent any
    
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
      ORIGIN_BRANCH_NAME = "${env.BRANCH_NAME}"
      ORIGIN_CHANGE_NAME = "${env.CHANGE_BRANCH}"
    }

    tools {
      maven 'M3'
    }
    
    stages {
        stage('Build') {
            steps {
                echo 'Building...'
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                echo "BranchName: ${ORIGIN_BRANCH_NAME}"
                echo "ChangeBranch: ${ORIGIN_CHANGE_NAME}"
                echo "${GIT_USER}"
                echo "${GIT_AUTHOR_NAME}"
                echo "${GIT_AUTHOR_EMAIL}"
                echo "${GIT_COMMITTER_NAME}"
                echo "${GIT_COMMITTER_EMAIL}"
                echo "${GIT_COMMIT}"
                echo "${GIT_URL}"
                echo "${GIT_BRANCH}"

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
