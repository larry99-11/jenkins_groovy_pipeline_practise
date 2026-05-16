/*
So the full picture is:
jenkins-shared-library repo          your-go-app repo
────────────────────────────         ────────────────
vars/
  buildAppChart.groovy  ←────────── Jenkinsfile
  (full pipeline lives here)         (@Library + one line call)
*/
import org.helpers.Docker
//import org.helpers.Helm

def call() {

    def docker = new Docker(this)
    //def helm = new Helm(this)

    pipeline {

        agent any

        environment {
             KUBE_NAMESPACE         = ''
             HELM_CHART_DIR         = ''
             IMAGE_NAME             = ''
             DOCKERHUB_USER         = ''
             DOCKERHUB_CREDENTIALS  = ''
             IMAGE_TAG              = ''
             GIT_SHORT_SHA          = ''
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
                        GIT_SHORT_SHA = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                        IMAGE_TAG = "${DOCKERHUB_USER}/${IMAGE_NAME}:${GIT_SHORT_SHA}"
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

            stage('Docker build') {
                steps {
                    script {
                        docker.docker_build(IMAGE_TAG) 
                    }                   
                }
            }

            stage('Docker push') {
                steps {
                    script {
                        docker.docker_push(IMAGE_TAG, DOCKERHUB_CREDENTIALS)
                    }
                }
            }
        }
    
    // clean up the image locally via removing it and log out
        post {
            always {
                script {
                    docker.docker_cleanup(IMAGE_TAG)
                }
            }
        }

    }
}