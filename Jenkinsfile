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
        stage('Test') {
            steps {
                echo 'Testing...'
                sh 'mvn test'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
            }
        }
    }
}
