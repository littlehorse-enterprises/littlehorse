import getPropertiesArgs, { ConfigArgs } from './getPropertiesArgs'

describe('getPropertiesArgs', () => {
  it('should return partial configuration', async () => {
    const args: Partial<ConfigArgs> = {
      apiHost: 'localhost',
      apiPort: '2023',
      protocol: 'TLS',
      tenantId: 'example',
      caCert: '/path/to/cert.crt',
      clientCert: '/path/to/client.crt',
      clientKey: '/path/to/client.key',
    }
    const properties = getPropertiesArgs(args)

    expect(properties).toStrictEqual({
      LHC_API_HOST: 'localhost',
      LHC_API_PORT: '2023',
      LHC_API_PROTOCOL: 'TLS',
      LHC_TENANT_ID: 'example',
      LHC_CA_CERT: '/path/to/cert.crt',
      LHC_CLIENT_CERT: '/path/to/client.crt',
      LHC_CLIENT_KEY: '/path/to/client.key',
    })
  })
})
