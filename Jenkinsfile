pipeline {
    options { 
      buildDiscarder(logRotator(numToKeepStr: '3', artifactNumToKeepStr:'3'))
      timeout(time: 1, unit: 'HOURS')
      skipStagesAfterUnstable()
      retry(1)
      parallelsAlwaysFailFast()
    }
    agent { label 'docker' }
    environment {
      ARTIFACT_ID = readMavenPom().getArtifactId()
      PROJECT_VERSION = readMavenPom().getVersion()
      // SECRET = vault path: 'secrets', key: 'password'
      // USERNAME = vault path: 'secrets', key: 'username'
      // VALUES = vault path: 'secrets', key: 'values-yaml'
    }
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
    }
}