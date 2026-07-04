/*
Dependencies -> Helm and Docker classes
So the full picture is:
jenkins-shared-library repo          your-go-app repo
────────────────────────────         ────────────────
vars/
  buildAppChart.groovy  ←────────── Jenkinsfile
  (full pipeline lives here)         (@Library + one line call)
*/
import org.helpers.Docker
import org.helpers.Helm

def call() {
    // Initialising utiliy classes
    def docker = new Docker(this)
    def helm = new Helm(this)

    pipeline {

        agent any

        // Defining environment 
        environment {
             KUBE_NAMESPACE         = ''
             HELM_CHART_DIR         = ''
             IMAGE_NAME             = ''
             DOCKERHUB_USER         = ''
             DOCKERHUB_CREDENTIALS  = ''
             IMAGE_TAG              = ''
             GIT_SHORT_SHA          = ''
             DOCKERFILE_PATH        = ''    
             BUILD_CONTEXT          = ''
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
                        DOCKERFILE_PATH        = props.DOCKERFILE_PATH   
                        BUILD_CONTEXT          = props.BUILD_CONTEXT

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

            stage('Docker build') {
                steps {
                    script {
                        docker.docker_build(IMAGE_TAG,DOCKERFILE_PATH, BUILD_CONTEXT) 
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

            stage('Helm Deploy') {
                steps {
                    script {
                        helm.deploy(IMAGE_NAME, HELM_CHART_DIR, "${DOCKERHUB_USER}/${IMAGE_NAME}", GIT_SHORT_SHA, KUBE_NAMESPACE)
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