import getProperties from './getProperties'

describe('getProperties', () => {
  it('should return partial configuration', async () => {
    const properties = getProperties('fixtures/littlehorse.config')

    expect(properties).toStrictEqual({
      LHC_API_HOST: 'localhost',
      LHC_API_PORT: '2023',
    })
  })
})
