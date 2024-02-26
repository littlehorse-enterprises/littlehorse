import getPropertiesArgs from './getPropertiesArgs'

describe('getPropertiesArgs', () => {
  it('should return partial configuration', async () => {
    const args = {
      apiHost: 'localhost',
      apiPort: '2023',
      protocol: 'SSL',
      tenantId: 'example',
      caCert: '-----BEGIN CERTIFICATE-----CERTIFICATE CONTENT-----END CERTIFICATE-----',
    }
    const properties = getPropertiesArgs(args)

    expect(properties).toStrictEqual({
      LHC_API_HOST: 'localhost',
      LHC_API_PORT: '2023',
      LHC_API_PROTOCOL: 'SSL',
      LHC_TENANT_ID: 'example',
      LHC_CA_CERT: '-----BEGIN CERTIFICATE-----CERTIFICATE CONTENT-----END CERTIFICATE-----',
    })
  })
})
