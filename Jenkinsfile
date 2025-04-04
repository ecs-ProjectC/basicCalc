pipeline {
    agent {
        label 'maven'  // This will run on either Jen_node1 or Jen_node2, as both have the 'maven' label
    }

    stages {
        stage('Pull Docker Image') {
            steps {
                script {
                    // Define the Docker image from JFrog
                    dockerImage = 'trial3p0e6v.jfrog.io/devops-dockervirtual/jenkins_custom:latest'
                    
                    // Pull the Docker image on the node
                    sh "docker pull ${dockerImage}"
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
                        ${dockerImage}
                    """
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

