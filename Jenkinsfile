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
                script {
                    if (fileExists('frontend/package.json')) {
                        dir('frontend') {
                            sh 'npm install'
                            // Run tests using the local ng installed by npm
                            sh 'npm run test -- --watch=false --browsers=ChromeHeadlessNoSandbox'
                        }
                    } else {
                        echo 'Skipping frontend tests: frontend directory not found'
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    def scannerHome = tool 'SonarQubeScanner'
                    def sonarParams = ""
                    if (!fileExists('frontend')) {
                         echo 'Frontend directory not found. Excluding from SonarQube analysis.'
                         sonarParams = "-Dsonar.sources=services"
                    }

                    withSonarQubeEnv('SonarQube') {
                        sh "${scannerHome}/bin/sonar-scanner ${sonarParams}"
                    }
                    timeout(time: 1, unit: 'HOURS') {
                        waitForQualityGate abortPipeline: false
                    }
                }
            }
        }

        stage('Build Images') {
            steps {
                sh 'docker compose build'
            }
        }

        stage('Deploy') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'main'
                }
            }
            steps {
                // Deploy locally by restarting containers
                sh 'docker compose up -d'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline succeeded!'
            script {
               def emailTo = env.ADMIN_EMAIL ?: 'email@example.com'
               echo "Sending success email to ${emailTo}"
               mail to: emailTo,
                    subject: "✅ Build Succeeded: ${currentBuild.fullDisplayName}",
                    body: """
                    Great news! The build was successful.
                    
                    Project: ${env.JOB_NAME}
                    Build Number: ${env.BUILD_NUMBER}
                    URL: ${env.BUILD_URL}
                    """
            }
        }
        failure {
            echo 'Pipeline failed!'
            script {
               def emailTo = env.ADMIN_EMAIL ?: 'email@example.com'
               echo "Sending failure email to ${emailTo}"
               mail to: emailTo,
                    subject: "❌ Build Failed: ${currentBuild.fullDisplayName}",
                    body: """
                    The build has failed. Please investigate.
                    
                    Project: ${env.JOB_NAME}
                    Build Number: ${env.BUILD_NUMBER}
                    URL: ${env.BUILD_URL}
                    
                    Check the console logs for errors.
                    """
            }
        }
    }
}
