targetScope = 'subscription'

// received from dev.bicepparam
param resourceGroupName string
param location string
param acrName string
param acrSku string


// aks params 
// AKS params
param aksResourceName string

param aksNodeVmSize string
param aksSku string
param aksTier string
param aksDnsPrefix string
param osDiskSizeGB int
param aksOsType string
param agentCount  int
param linuxAdminUsername string
@secure()
param sshRSAPublicKey string


// deploy resource group first
module rg '../modules/resource-group.bicep' = {
  name: 'Azure-resourcegroup'
  params: {
    location: location
    resourceGroupName: resourceGroupName 
  }
}
// deploy acr second as it depends on the 'rg' module
module acr '../modules/acr.bicep' = {
  name: acrName
  scope: resourceGroup(resourceGroupName) 
  params: {
    location: location
    acrName: acrName
    acrSku: acrSku
  }
  dependsOn: [rg]
}

module aks '../modules/aks.bicep' = {
  name: aksResourceName
  scope: resourceGroup(resourceGroupName)
  params: {
    aksResourceName:   aksResourceName
    location:          location
    aksSku:            aksSku
    aksTier:           aksTier
    aksDnsPrefix:      aksDnsPrefix
    osDiskSizeGB:      osDiskSizeGB
    aksNodeVmSize:     aksNodeVmSize
    agentCount:        agentCount
    aksOsType:         aksOsType
    linuxAdminUsername: linuxAdminUsername
    sshRSAPublicKey:   sshRSAPublicKey
  }
  dependsOn:[rg]
}
