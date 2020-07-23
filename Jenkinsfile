pipeline {

    agent any
    
    options { 
      buildDiscarder(logRotator(numToKeepStr: '1'))
      timeout(time: 1, unit: 'HOURS') 
      skipStagesAfterUnstable()
      retry(2)
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
            steps {
              failFash true
              parallel (
                JUnit: {
                  sh 'mvn test'
                },
                Contracts: {
                  echo 'Other types of parallel tests'
                }
              )
            }
        }
        stage('Deploy') {
            when {
              expression {
                currentBuild.result == null || currentBuild.result == 'SUCCESS' 
              }
            }
            steps {
                echo 'Deploying....'
            }
        }
    }
}
