pipeline {

    agent any
    
    options { 
      buildDiscarder(logRotator(numToKeepStr: '3', artifactNumToKeepStr:'3'))
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
        stage('Deploy') {
            when {
              expression {
                currentBuild.result == null || currentBuild.result == 'SUCCESS' 
              }
            }
            steps {
                echo 'Deploying....'
                script {
                  VERSION_INFORMATION = mavenSemanticVersion("readOnly":true)
                  ARTIFACT_ID = VERSION_INFORMATION.artifactId
                  PROJECT_VERSION = VERSION_INFORMATION.version
                  SEMANTIC_VERSION = VERSION_INFORMATION.versionShort
                  JAR_FILE_NAME = "target/hello-world.jar"
                  image = docker.build("melbin/my-repo:${SEMANTIC_VERSION}","-f Dockerfile --build-arg JAR_FILE='${JAR_FILE_NAME}'")
                  image.push()
                }
            }
        }
    }
}
