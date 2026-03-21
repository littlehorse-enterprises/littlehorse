import { LHConfig, toVariableValue } from 'littlehorse-client'

async function main() {
  const config = LHConfig.from({})
  const client = config.getClient()

  const quantity = Number(process.argv[2] ?? 2)
  const price = Number(process.argv[3] ?? 10)
  const taxes = Number(process.argv[4] ?? 8)

  console.log(`Running workflow "example-expressions" with quantity=${quantity}, price=${price}, taxes=${taxes}%...`)

  const result = await client.runWf({
    wfSpecName: 'example-expressions',
    variables: {
      quantity: toVariableValue(quantity),
      price: toVariableValue(price),
      taxes: toVariableValue(taxes),
    },
  })

  console.log(`Started WfRun: ${result.id?.id}`)
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
