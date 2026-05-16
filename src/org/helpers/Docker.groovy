package org.helpers

class Docker {

    def steps
    //pass in the pipeline steps so we can run sh, echo etc
    Docker(steps) {
        this.steps = steps
    }

    def docker_build(String imageTag) {
        steps.sh "docker build -t ${imageTag} ."
    }

    def docker_push(String imageTag, String credentialsId) {
        steps.withCredentials([steps.usernamePassword(
            credentialsId: credentialsId,
            usernameVariable: 'DH_USER', // <- Jenkins takes usernameVariable and passwordVariable which are the names of the environment variables set in Jenkins secrets
            passwordVariable: 'DH_PASS'
        )]) {
            steps.sh """
                echo \$DH_PASS | docker login -u \$DH_USER --password-stdin
                docker push ${imageTag}
            """
        }
    }

    def docker_cleanup(String imageTag) {
        steps.sh "docker rmi ${imageTag} || true"
        steps.sh "docker logout || true"
    }
}