pipeline {
    agent any
    environment {
        COMPOSE_FILE = 'docker-compose.yml'
        COMPOSE_PROJECT_NAME = 'mr-jenk'
    }
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code...'
                checkout scm
            }
        }
        stage('Build System Check') {
            steps {
                sh 'docker compose version'
                echo 'Build system is ready.'
            }
        }
    }
    post {
        success {
            echo 'Pipeline initialized successfully!'
        }
    }
}
