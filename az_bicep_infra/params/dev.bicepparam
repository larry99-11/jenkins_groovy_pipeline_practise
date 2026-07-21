// Points to our main.bicep file
using '../dev/main.bicep'

// Resource group params
param resourceGroupName = 'rg-gowebapp-dev'
param location = 'uksouth'

// AKS params
//param nodeVmSize = 'Standard_B2s'

// ACR params
param acrName = 'acrgowebserverexample'
param acrSku = 'Basic'

