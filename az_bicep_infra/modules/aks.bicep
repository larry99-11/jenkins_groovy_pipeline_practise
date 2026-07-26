param aksResourceName string
param location string
param aksSku string
param aksTier string
param aksDnsPrefix string
param osDiskSizeGB int
param aksNodeVmSize string
param agentCount int
param aksOsType string
param linuxAdminUsername string
param sshRSAPublicKey string

resource aks 'Microsoft.ContainerService/managedClusters@2024-09-01' = { 
  name: aksResourceName
  location: location
  identity: {
    type: 'SystemAssigned'
  }
  sku: {
    name: aksSku
    tier: aksTier
  }

  properties: {
    dnsPrefix: aksDnsPrefix
    agentPoolProfiles: [
      {
        name: 'akspool01'
        osDiskSizeGB: osDiskSizeGB
        count: agentCount
        vmSize: aksNodeVmSize
        osType: aksOsType
        mode: 'System'
      }
    ]
    linuxProfile: {
      adminUsername: linuxAdminUsername
      ssh: {
        publicKeys: [
          {
            keyData: sshRSAPublicKey
          }
        ]
      }
    }
  }
}

output controlPlaneFQDN string = aks.properties.fqdn
output aksName string = aks.name
