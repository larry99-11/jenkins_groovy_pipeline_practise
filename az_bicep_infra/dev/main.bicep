targetScope = 'subscription'

// received from dev.bicepparam
param resourceGroupName string
param location string
param acrName string
param acrSku string

//param aksClusterName string
//param nodeVmSize string

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
