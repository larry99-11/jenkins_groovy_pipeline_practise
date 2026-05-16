/*
So the full picture is:
jenkins-shared-library repo          your-go-app repo
────────────────────────────         ────────────────
vars/
  buildAppChart.groovy  ←────────── Jenkinsfile
  (full pipeline lives here)         (@Library + one line call)
*/


def call() {
    pipeline {

        agent any

        environment {
            KUBE_NAMESPACE         = ''
            HELM_CHART_DIR         = ''
            IMAGE_NAME             = ''
            DOCKERHUB_USER         = ''
            DOCKERHUB_CREDENTIALS  = ''
        }

        stages {
            stage('Load Config') {
                steps {
                    script {
                        def props = readProperties file: 'pipeline.properties'
                        KUBE_NAMESPACE         = props.KUBE_NAMESPACE
                        HELM_CHART_DIR         = props.HELM_CHART_DIR
                        IMAGE_NAME             = props.IMAGE_NAME
                        DOCKERHUB_USER         = props.DOCKERHUB_USER
                        DOCKERHUB_CREDENTIALS  = props.DOCKERHUB_CREDENTIALS

                    }
                }
            }

            stage('Checkout') {
                steps {
                    checkout scm
                    script {
                        env.GIT_SHORT_SHA = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                        env.IMAGE_TAG = "${DOCKERHUB_USER}/${IMAGE_NAME}:${GIT_SHORT_SHA}"
                    }
                }
            }

            stage('Debug') {
                steps {
                    echo "KUBE_NAMESPACE:        ${KUBE_NAMESPACE}"
                    echo "HELM_CHART_DIR:        ${HELM_CHART_DIR}"
                    echo "IMAGE_NAME:            ${IMAGE_NAME}"
                    echo "DOCKERHUB_USER:        ${DOCKERHUB_USER}"
                    echo "DOCKERHUB_CREDENTIALS: ${DOCKERHUB_CREDENTIALS}"
                    echo "GIT_SHORT_SHA:         ${GIT_SHORT_SHA}"
                    echo "IMAGE_TAG:             ${IMAGE_TAG}"
                }
            }
        }

    }
}