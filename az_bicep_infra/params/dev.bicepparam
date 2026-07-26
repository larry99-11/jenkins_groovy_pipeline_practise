// Points to our main.bicep file
using '../dev/main.bicep'

// Resource group params
param resourceGroupName = 'rg-gowebapp-dev'
param location = 'ukwest'

// AKS params
param aksResourceName = 'aksresourcekube'

param aksNodeVmSize = 'Standard_D2s_v3'
param aksSku = 'Base'
param aksTier = 'Free'
param aksDnsPrefix = 'aksdns'
param osDiskSizeGB = 0 // 0 will just default to the standard disk size
param aksOsType = 'Linux'
param agentCount  = 1
param linuxAdminUsername = 'goAdmin'
param sshRSAPublicKey = ''

// ACR params
param acrName = 'acrgowebserverexample'
param acrSku = 'Basic'

