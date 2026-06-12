import { LHConfig, toVariableValue } from 'littlehorse-client'

async function main() {
  const config = LHConfig.from({})
  const client = config.getClient()

  const inputText = process.argv[2] ?? 'hello'
  const addLength = process.argv[3] ? process.argv[3] === 'true' : true
  const userId = process.argv[4] ? Number(process.argv[4]) : 42

  const result = await client.runWf({
    wfSpecName: 'example-variables',
    variables: {
      'input-text': toVariableValue(inputText),
      'add-length': toVariableValue(addLength),
      'user-id': toVariableValue(userId),
    },
  })

  console.log(`Started WfRun: ${result.id?.id}`)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
