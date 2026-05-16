#building off from jenkins/jenkins:lts-jdk21 base img
FROM jenkins/jenkins:lts

RUN jenkins-plugin-cli --plugins \
    docker-workflow \
    kubernetes \
    pipeline-stage-view \
    git \
    credentials-binding \
    blueocean


