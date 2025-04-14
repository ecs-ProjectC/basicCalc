pipeline {
    agent none  // Define agents per stage for CI/CD separation

    parameters {
        booleanParam(name: 'DEPLOY_RELEASE', defaultValue: false, description: 'Deploy release branch to staging?')
    }

    environment {
        GIT_REPO = 'https://github.com/ecs-ProjectC/basicCalc.git'
        IMAGE_NAME = 'basiccalc-app'
        CONTAINER_NAME = 'basiccalc-release-container'
        APP_PORT = '5000'
    }

    stages {

        stage('Checkout Code') {
            agent { label 'maven' }
            steps {
                checkout scm
                script {
                    echo " Checked out branch: ${env.BRANCH_NAME} from ${env.GIT_REPO}"
                }
            }
        }

        stage('Build with Maven') {
            agent { label 'maven' }
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Run Unit Tests') {
            agent { label 'maven' }
            steps {
                sh 'mvn test'
            }
        }

        stage('SonarCloud Analysis') {
            when {
                expression {
                    return env.BRANCH_NAME == 'main' || env.BRANCH_NAME.startsWith('release/')
                }
            }
            agent { label 'maven' }
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    sh """
                    mvn sonar:sonar \
                        -Dsonar.projectKey=basicCalc \
                        -Dsonar.organization=thrijwal \
                        -Dsonar.host.url=https://sonarcloud.io \
                        -Dsonar.login=$SONAR_TOKEN
                    """
                }
            }
        }

        stage('Build Docker Image') {
            when {
                expression {
                    return env.BRANCH_NAME == 'main' || env.BRANCH_NAME.startsWith('release/')
                }
            }
            agent { label 'deployment_server' }
            steps {
                sh "docker build -t ${IMAGE_NAME}:${env.BRANCH_NAME} ."
            }
        }

        stage('Deploy Release to Staging') {
            when {
                allOf {
                    expression { env.BRANCH_NAME.startsWith('release/') }
                    expression { params.DEPLOY_RELEASE == true }
                }
            }
            agent { label 'deployment_server' }
            steps {
                sh """
                echo "Deploying release to staging..."
                docker stop ${CONTAINER_NAME} || true
                docker rm ${CONTAINER_NAME} || true
                docker run -dit --name ${CONTAINER_NAME} \
                    -p ${APP_PORT}:8080 \
                    -v /home/ubuntu/basicCalc/logs:/app/logs \
                    ${IMAGE_NAME}:${env.BRANCH_NAME}
                """
            }
        }

        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            agent { label 'deployment_server' }
            steps {
                sh """
                echo " Deploying main branch to production..."
                docker stop ${CONTAINER_NAME} || true
                docker rm ${CONTAINER_NAME} || true
                docker run -dit --name ${CONTAINER_NAME} \
                    -p ${APP_PORT}:8080 \
                    -v /home/ubuntu/basicCalc/logs:/app/logs \
                    ${IMAGE_NAME}:${env.BRANCH_NAME}
                """
            }
        }
    }

    post {
        always {
            agent { label 'deployment_server' }
            steps {
                echo "Cleaning up unused Docker resources..."
                sh 'docker container prune -f || true'
                sh 'docker image prune -f || true'
            }
        }

        success {
            echo " Pipeline completed for branch: ${env.BRANCH_NAME}"
        }

        failure {
            echo " Pipeline failed for branch: ${env.BRANCH_NAME}"
        }
    }
}
