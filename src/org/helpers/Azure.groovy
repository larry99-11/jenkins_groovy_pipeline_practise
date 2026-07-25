package org.helpers
// We want in this package is: push images to ACR -> can deploy new resources in Azure

class Azure {

    def steps

    Azure(steps) {
        this.steps = steps
    }

    def acr_push(String imageTag, String credentialsId, String acrName) {
        steps.withCredentials([steps.usernamePassword(
            credentialsId: credentialsId,
            usernameVariable: 'ACR_USER', // <- Jenkins takes usernameVariable and passwordVariable which are the names of the environment variables set in Jenkins secrets
            passwordVariable: 'ACR_PASS'
        )]) {
            steps.sh """
                echo \$ACR_PASS | docker login ${acrName}.azurecr.io \
                    -u \$ACR_USER \
                    --password-stdin
                docker push ${imageTag}
            """
        }
    }

    def acr_cleanup(String imageTag, String acrName) {
        steps.sh """
            docker rmi ${imageTag} || true
            docker logout ${acrName}.azurecr.io || true
        """
    }    
}