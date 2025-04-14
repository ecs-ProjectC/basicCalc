pipeline {
    agent none

    parameters {
        booleanParam(name: 'DEPLOY_RELEASE', defaultValue: false, description: 'Deploy release branch to staging?')
    }

    environment {
        GIT_REPO = 'https://github.com/ecs-ProjectC/basicCalc.git'
        IMAGE_NAME = 'thrijwaldockerboy/ecs-projc'
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

        stage('Build & Push Docker Image') {
            when {
                expression {
                    return env.BRANCH_NAME == 'main' || env.BRANCH_NAME.startsWith('release_')
                }
            }
            agent { label 'maven' }
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                    echo " Building Docker image..."
                    docker build -t ${IMAGE_NAME}:${env.BRANCH_NAME} .

                    echo " Logging into Docker Hub..."
                    echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

                    echo " Pushing image to Docker Hub..."
                    docker push ${IMAGE_NAME}:${env.BRANCH_NAME}

                    docker logout
                    """
                }
            }
        }

        stage('Deploy to Production') {
            when {
                allOf {
                    expression { env.BRANCH_NAME.startsWith('release_') }
                }
            }
            agent { label 'deployment_server' }
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                    echo " Logging into Docker Hub..."
                    echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

                    echo " Pulling image for release: ${IMAGE_NAME}:${env.BRANCH_NAME}"
                    docker pull ${IMAGE_NAME}:${env.BRANCH_NAME}

                    echo " Deploying release to staging..."
                    docker stop ${CONTAINER_NAME} || true
                    docker rm ${CONTAINER_NAME} || true
                    docker run -dit --name ${CONTAINER_NAME} \
                        -p ${APP_PORT}:8080 \
                        -v /var/jenkins_home:/app \
                        -v /var/run/docker.sock:/var/run/docker.sock \
                        ${IMAGE_NAME}:${env.BRANCH_NAME}

                    docker logout
                    """
                }
            }
        }

    }

    post {
        always {
            script {
                node('maven') {
                    if (env.NODE_LABELS?.contains('maven')) {
                        echo " Cleaning up Docker resources on build agent..."
                        sh 'docker container prune -f || true'
                        sh 'docker image prune -f || true'
                    } else {
                        echo " Skipping Docker cleanup — not on build agent."
                    }
                }
            }
        }

        success {
            echo " Pipeline completed successfully for branch: ${env.BRANCH_NAME}"
        }

        failure {
            echo " Pipeline failed for branch: ${env.BRANCH_NAME}"
        }
    }
}
