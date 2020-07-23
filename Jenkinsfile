pipeline {
    agent any
    
    tools {
      maven 'M3'
    }
    
    stages {
        stage('Build') {
            steps {
                echo 'Building...'
                sh 'mvn -B -DskipTests clean package' 
                archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
            }
        }
        stage('Parallel Tests') {
            steps {
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
            steps {
                echo 'Deploying....'
            }
        }
    }
}
