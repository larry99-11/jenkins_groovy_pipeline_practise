#building off from jenkins/jenkins:lts-jdk21 base img
FROM jenkins/jenkins:lts

# Jenkins agnet  needs docker cli to run docker cmds in the pipeline
RUN apt-get update && apt-get install -y docker.io

RUN jenkins-plugin-cli --plugins \
    docker-workflow \
    kubernetes \
    pipeline-stage-view \
    git \
    credentials-binding \
    blueocean \
    pipeline-utility-steps \
    workflow-aggregator \


