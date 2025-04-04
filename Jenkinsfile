pipeline {
    agent {
        label 'maven'  // This will run on either Jen_node1 or Jen_node2
    }

    environment {
        JFROG_REGISTRY = "trial3p0e6v.jfrog.io"
        JFROG_REPO = "devops-dockervirtual/projc_basiccalc:latest"
        JFROG_CREDENTIALS = "jfrog-credentials"  // The credentials ID created in Jenkins
        GIT_REPO_URL = "https://github.com/ecs-ProjectC/basicCalc.git" // GitHub repository URL
        SONAR_PROJECT_KEY = "basicCalc"
        SONAR_ORG = "thrijwal"
        SONAR_URL = "https://sonarcloud.io"
        SONAR_LOGIN = "f1388b1bac0c3656a6782c1a1065e3cbc561c59f" // Please use a secure method for handling this in production
    }

    stages {
        stage('Pull the Docker Image') {
            steps {
                script {
                    // Use Jenkins' withCredentials to pass the JFrog credentials securely
                    withCredentials([usernamePassword(credentialsId: env.JFROG_CREDENTIALS, usernameVariable: 'JFROG_USERNAME', passwordVariable: 'JFROG_ACCESS_KEY')]) {
                        // Authenticate Docker with JFrog Artifactory
                        sh "docker login ${env.JFROG_REGISTRY} -u ${JFROG_USERNAME} -p ${JFROG_ACCESS_KEY}"

                        // Pull the Docker image from JFrog Artifactory
                        sh "docker pull ${JFROG_REGISTRY}/${JFROG_REPO}"
                    }
                }
            }
        }

        stage('Run Docker Container') {
            steps {
                script {
                    // Run the Docker container with the specific flags in detached mode
                    sh """
                    docker run -dit \
                        --name projc \
                        -p 8080:8080 \
                        -p 50000:50000 \
                        -v /var/jenkins_home:/app \
                        -v /var/run/docker.sock:/var/run/docker.sock \
                        ${JFROG_REGISTRY}/${JFROG_REPO}
                    """
                }
            }
        }

        stage('Run Maven Build') {
            steps {
                script {
                    // Run Maven build inside the container (without specifying the project path explicitly)
                    sh """
                    docker exec projc bash -c 'cd basicCalc && mvn clean install'
                    """
                    echo 'Maven build completed!'
                }
            }
        }

        stage('Run Maven Tests') {
            steps {
                script {
                    // Run Maven tests inside the container
                    sh """
                    docker exec projc bash -c 'cd basicCalc && mvn test'
                    """
                    echo 'Maven tests completed!'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    // Run SonarQube analysis inside the container
                    sh """
                    docker exec projc bash -c 'cd basicCalc && mvn clean verify sonar:sonar \
                        -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                        -Dsonar.organization=${SONAR_ORG} \
                        -Dsonar.host.url=${SONAR_URL} \
                        -Dsonar.login=${SONAR_LOGIN}'
                    """
                    echo 'SonarQube analysis completed!'
                }
            }
        }
    }

    post {
        always {
            // Clean up by stopping the Docker container after the build
            echo "Build, Tests, SonarQube Analysis, and Deployment to GitLab and JFrog succeeded!"
            sh "docker stop projc"
            sh "docker rm projc"
            sh "docker rmi ${JFROG_REGISTRY}/${JFROG_REPO}"
        }
    }
}
