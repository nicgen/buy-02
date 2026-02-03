pipeline {
    agent any

    environment {
        COMPOSE_FILE = 'docker-compose.yml'
        COMPOSE_PROJECT_NAME = 'mr-jenk'
    }

    stages {
        stage('Test Backend') {
            steps {
                script {
                    echo 'Testing User Service...'
                    dir('services/user-service') {
                        sh 'mvn test'
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'User Service verified successfully!'
        }
    }
}
