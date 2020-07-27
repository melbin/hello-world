pipeline {
    agent none
    options { 
      // buildDiscarder(logRotator(numToKeepStr: '3', artifactNumToKeepStr:'3'))
      // timeout(time: 1, unit: 'HOURS')
      // skipStagesAfterUnstable()
      // retry(1)
      // parallelsAlwaysFailFast()
    }
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
                // archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
              }
            }
          }
        }
    }
}