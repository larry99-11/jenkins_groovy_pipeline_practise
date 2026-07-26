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
import org.helpers.Azure

def call() {
    //Have the option to choose local or aks deployment
    pipelineParams()

    // Initialising utiliy classes
    def docker = new Docker(this)
    def helm = new Helm(this)
    def azure = new Azure(this)

    pipeline {

        agent any

    }

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
             ACR_NAME               = ''
             ACR_CREDENTIALS        = ''
             ACR_IMAGE_TAG          = ''
        }

        stages {
            stage('Load Config') {
                steps {
                    script {
                        def props = readProperties file: 'pipeline.properties'
                        // Selects appropriate namespace based on params selected
                        KUBE_NAMESPACE         = params.ENVIRONMENT == 'aks' ? props.AKS_KUBE_NAMESPACE : props.LOCAL_KUBE_NAMESPACE
                        HELM_CHART_DIR         = props.HELM_CHART_DIR
                        IMAGE_NAME             = props.IMAGE_NAME
                        DOCKERHUB_USER         = props.DOCKERHUB_USER
                        DOCKERHUB_CREDENTIALS  = props.DOCKERHUB_CREDENTIALS
                        DOCKERFILE_PATH        = props.DOCKERFILE_PATH   
                        BUILD_CONTEXT          = props.BUILD_CONTEXT
                        ACR_NAME               = props.ACR_NAME 
                        ACR_CREDENTIALS        = props.ACR_CREDENTIALS
                        ACR_IMAGE_TAG          = props.ACR_IMAGE_TAG

                    }
                }
            }

            stage('Checkout') {
                steps {
                    checkout scm
                    script {
                        GIT_SHORT_SHA = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                        IMAGE_TAG = "${DOCKERHUB_USER}/${IMAGE_NAME}:${GIT_SHORT_SHA}"
                        ACR_IMAGE_TAG ="${ACR_NAME}.azurecr.io/${IMAGE_NAME}:${GIT_SHORT_SHA}"

                    }
                }
            }
            // ── Build Docker image (both flows need this) ──────────────────
            stage('Docker build') {
                steps {
                    script {
                        docker.docker_build(IMAGE_TAG,DOCKERFILE_PATH, BUILD_CONTEXT) 
                    }                   
                }
            }
            // ── if local param → DockerHub, if aks param → ACR ────────────────────────
            stage('Push image') {
                steps {
                    script {
                        if (params.ENVIRONMENT == 'local') {
                            echo "Pushing image to Dockerhub..."
                            docker.docker_push(IMAGE_TAG, DOCKERHUB_CREDENTIALS)
                        } else if (params.ENVIRONMENT == 'aks') {
                            azure.acr_push(IMAGE_TAG, ACR_IMAGE_TAG, ACR_CREDENTIALS, ACR_NAME)
                        }
                    }
                }
            }
            // ── if aks → verify cluster is up ─────────────────────────────
            stage('Verify Cluster') {
                steps {
                    script {
                        if (params.ENVIRONMENT == 'aks') {
                            echo "Verifying AKS cluster..."
                            sh '''
                                kubectl config use-context aksresourcekube
                                kubectl cluster-info
                                kubectl get nodes
                                READY_NODES=$(kubectl get nodes --no-headers | grep -c "Ready")
                                
                                if [ "$READY_NODES" -eq "0" ]; then
                                    echo "❌ No Ready nodes found"
                                    exit 1
                                fi
                                
                                echo "✅ ${READY_NODES} node(s) Ready"  
                            '''
                        } else {
                            echo "Verifying local Kind cluster..."
                            sh '''
                                kubectl config use-context kind-personal-dev
                                kubectl cluster-info
                                kubectl get nodes
                            '''
                        }
                    }
                }
            }
            stage('Helm Deploy') {
                steps {
                    script {
                        if (params.ENVIRONMENT == 'local') {
                            echo "Deploying to local kind cluster..."
                            helm.deploy(IMAGE_NAME, HELM_CHART_DIR, "${DOCKERHUB_USER}/${IMAGE_NAME}", GIT_SHORT_SHA, KUBE_NAMESPACE, 'ClusterIP') // we can pramameterise this at the top
                        } else if (params.ENVIRONMENT == 'aks') {
                            echo "Deploying to AKS..."
                            helm.deploy(IMAGE_NAME, HELM_CHART_DIR, "${ACR_NAME}.azurecr.io/${IMAGE_NAME}", GIT_SHORT_SHA, KUBE_NAMESPACE, 'LoadBalancer')
                        }
                    }
                }
            }
        }
    
    // clean up the image locally via removing it and log out
        post {
            always {
                script {
                    docker.docker_cleanup(IMAGE_TAG)
                    if (params.ENVIRONMENT == 'aks') {
                        azure.acr_cleanup(ACR_IMAGE_TAG, ACR_NAME)
                    }
                }
            }
        }

}