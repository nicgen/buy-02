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

        stage('Test Frontend') {
            environment {
                CHROME_BIN = '/usr/bin/google-chrome-stable'
            }
            steps {
                dir('frontend') {
                    sh 'npm install'
                    sh 'npm run test -- --watch=false --browsers=ChromeHeadlessNoSandbox'
                }
            }
        }
    }

    post {
        success {
            echo 'Full stack verification successful!'
        }
    }
}
