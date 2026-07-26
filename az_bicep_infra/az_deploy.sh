#!/bin/bash
LOCATION=${1:-"uksouth"}
TEMP_FILE_LOCATION=${2:-"dev/main.bicep"}
PARAMS_FILE_LOCATION=${3:-"params/dev.bicepparam"}
SSH_KEY=${4:-""}

if [ -z "$SSH_KEY" ]; then
  SSH_KEY=$(cat ~/.ssh/aks-key.pub)
fi

# Log into azure
#az login
# Deploy cmd
az deployment sub create \
  --location ${LOCATION} \
  --template-file ${TEMP_FILE_LOCATION} \
  --parameters ${PARAMS_FILE_LOCATION} \
  --parameters sshRSAPublicKey="$SSH_KEY"

# add some functionaluty to view images in ACR??


# genereate ssh Key pair for aks cluster
#ssh-keygen -t rsa -b 4096 -f ~/.ssh/aks-key


# get generated kube config
#az aks get-credentials \
#  --name aksresourcekube \
#  --resource-group rg-gowebapp-dev

# deleting aks
#az aks delete \
#  --name aksresourcekube \
#  --resource-group rg-gowebapp-dev \
#  --yes