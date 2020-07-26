pipeline {

    agent any
    
    options { 
      buildDiscarder(logRotator(numToKeepStr: '3', artifactNumToKeepStr:'3'))
      timeout(time: 1, unit: 'HOURS') 
      skipStagesAfterUnstable()
      retry(1)
      parallelsAlwaysFailFast()
    }
 
    tools {
      maven 'M3'
    }
    
    stages {
        stage('Build') {
            steps {
                echo 'Building...'
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
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
                    sh 'echo "${REPOSITORY_NAME}"'
                    VERSION_INFORMATION = mavenSemanticVersion("readOnly": true)
                    ARTIFACT_ID = VERSION_INFORMATION.artifactId
                    PROJECT_VERSION = VERSION_INFORMATION.version
                    SEMANTIC_VERSION = VERSION_INFORMATION.versionShort
                    JAR_FILE_NAME = "target/${ARTIFACT_ID}-${PROJECT_VERSION}.jar"
                    sh 'echo "${ARTIFACT_ID}"'
                    sh 'echo "${PROJECT_VERSION}"'
                    sh 'echo "${SEMANTIC_VERSION}"'
                    sh 'echo "${JAR_FILE_NAME}"'
                    image = docker.build("melbin/hello-world:'${SEMANTIC_VERSION}${RELEASE_PREFIX}'","-f Dockerfile --build-arg JAR_FILE='${JAR_FILE_NAME}' .")
                    image.push()
                }
            }
        }
        stage('Kubernates Deploy') {
          environment {
            KUBECONFIG = "/tmp/configs/kubeconfig"
          }
          steps {
            echo 'Deploying to kubernates PENDING...'
            script {
              DEPLOY_FILE = "deployment.yaml"
              SERVICE_FILE = "service.yaml"
              sh "kubectl apply -f '${DEPLOY_FILE}'"
              sh "kubectl apply -f '${SERVICE_FILE}'"
            }
          }
        }
    }
}
