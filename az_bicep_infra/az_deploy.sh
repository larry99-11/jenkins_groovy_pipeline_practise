#!/bin/bash
LOCATION=${1:-"uksouth"}
TEMP_FILE_LOCATION=${2:-"dev/main.bicep"}
PARAMS_FILE_LOCATION=${3:-"params/dev.bicepparam"}
# Log into azure
azure login
# Deploy cmd
az deployment sub create \
  --location ${LOCATION} \
  --template-file ${TEMP_FILE_LOCATION} \
  --parameters ${PARAMS_FILE_LOCATION}