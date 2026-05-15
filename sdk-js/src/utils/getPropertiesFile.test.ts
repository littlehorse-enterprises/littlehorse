import getPropertiesFile from './getPropertiesFile'

describe('getPropertiesFile', () => {
  it('should return partial configuration', async () => {
    const properties = getPropertiesFile('fixtures/littlehorse.config')

    expect(properties).toStrictEqual({
      LHC_API_HOST: 'localhost',
      LHC_API_PORT: '2023',
      LHC_API_PROTOCOL: 'TLS',
      LHC_GRPC_RESOURCE_EXHAUSTED_RETRY: 'false',
      LHC_TENANT_ID: 'example',
      LHC_CLIENT_CERT: '/path/to/client.crt',
      LHC_CLIENT_KEY: '/path/to/client.key',
      LHC_CA_CERT: '/path/to/cert.crt',
    })
  })
})
