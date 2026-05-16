package org.helpers

class Helm {

    def steps 
    Helm(steps) {
        this.steps = steps
    }

    def deploy(String releaseName, String chartDir, String imageRepo, String imageTag, String namespace){
        steps.sh """
            helm upgrade --install ${releaseName} ${chartDir} \
                --set image.repository=${imageRepo} \
                --set image.tag=${imageTag} \
                --namespace ${namespace} \
                --create-namespace \
                --wait
        """
    }

}