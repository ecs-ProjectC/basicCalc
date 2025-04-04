pipeline {
    agent {
        label 'maven'  // This will run on either Jen_node1 or Jen_node2
    }

    environment {
        JFROG_REGISTRY = "trial3p0e6v.jfrog.io"
        JFROG_REPO = "devops-dockervirtual/jenkins_custom:latest"
        JFROG_CREDENTIALS = "jfrog-credentials"  // The credentials ID created in Jenkins
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
                    docker run -d \
                        --name jenkins \
                        -p 8080:8080 \
                        -p 50000:50000 \
                        -v /var/jenkins_home:/var/jenkins_home \
                        -v /var/run/docker.sock:/var/run/docker.sock \
                        ${JFROG_REGISTRY}/${JFROG_REPO}
                    """
                }
            }
        }

        stage('Run Maven Build Inside Container') {
            steps {
                script {
                    // Run the Maven build inside the running container
                    //sh """
                    //docker exec jenkins mvn clean install  # Adjust this as needed for your build
                    //"""
                }
            }
        }
    }

    post {
        always {
            // Optionally clean up by stopping the Docker container after the build
            //sh "docker stop jenkins || true"  // Stop the container if it exists
            //sh "docker rm jenkins || true"    // Remove the container if it exists
        }
    }
}
