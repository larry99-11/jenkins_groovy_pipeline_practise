param acrName string 
param location string
param acrSku string


resource acr 'Microsoft.ContainerRegistry/registries@2023-07-01' = {
  name: acrName
  location: location
  sku: {
    name: acrSku
  }
}

output loginServer string = acr.properties.loginServer

