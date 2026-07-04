#building off from jenkins/jenkins:lts-jdk21 base img
FROM jenkins/jenkins:lts
USER root
# Jenkins agnet  needs docker cli to run docker cmds in the pipeline
RUN apt-get update && apt-get install -y docker.io

# Install Helm
RUN curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Add jenkins user to the docker group so it can access the socket
RUN groupadd -f docker && usermod -aG docker jenkins

#Swtich back to jenkins user
USER jenkins
RUN jenkins-plugin-cli --plugins \
    docker-workflow \
    kubernetes \
    pipeline-stage-view \
    git \
    credentials-binding \
    blueocean \
    pipeline-utility-steps \
    workflow-aggregator \


