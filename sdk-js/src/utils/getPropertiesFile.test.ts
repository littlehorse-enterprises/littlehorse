import getPropertiesFile from './getPropertiesFile'

describe('getPropertiesFile', () => {
  it('should return partial configuration', async () => {
    const properties = getPropertiesFile('fixtures/littlehorse.config')

    expect(properties).toStrictEqual({
      LHC_API_HOST: 'localhost',
      LHC_API_PORT: '2023',
      LHC_API_PROTOCOL: 'SSL',
      LHC_TENANT_ID: 'example',
      LHC_CA_CERT: '/path/to/cert.crt',
    })
  })
})
