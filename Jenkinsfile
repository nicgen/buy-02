pipeline {
    agent any

    environment {
        COMPOSE_FILE = 'docker-compose.yml'
        COMPOSE_PROJECT_NAME = 'mr-jenk'
    }

    stages {
        stage('Test Backend') {
            parallel {
                stage('User Service') {
                    steps {
                        dir('services/user-service') {
                            sh 'mvn test'
                        }
                    }
                }
                stage('Product Service') {
                    steps {
                        dir('services/product-service') {
                            sh 'mvn test'
                        }
                    }
                }
                stage('Media Service') {
                    steps {
                        dir('services/media-service') {
                            sh 'mvn test'
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Backend services verified successfully!'
        }
    }
}
