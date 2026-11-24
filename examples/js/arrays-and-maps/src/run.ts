import { LHStatus, WfRunId } from 'littlehorse-client/proto'
import { loadConfig } from './config'

const config = loadConfig()
type Client = Awaited<ReturnType<typeof config.getAuthenticatedClient>>

async function waitForRun(client: Client, id: WfRunId) {
  for (let i = 0; i < 150; i++) {
    const run = await client.getWfRun(id)
    if (run.status === LHStatus.COMPLETED || run.status === LHStatus.ERROR || run.status === LHStatus.EXCEPTION) {
      return run
    }
    await new Promise(r => setTimeout(r, 200))
  }
  throw new Error('WfRun did not finish in time')
}

async function main() {
  const client = await config.getAuthenticatedClient()
  // Typed collections are supplied as real LIST / MAP values. Assigning a plain
  // JS literal inside the workflow would compile to JSON_ARR / JSON_OBJ, and the
  // server rejects that with "Cannot assign JSON_ARR to null without explicit
  // casting" — a typed ARRAY and a JSON_ARR are genuinely different types here,
  // not two spellings of the same thing.
  const run = await client.runWf({
    wfSpecName: 'example-arrays-and-maps',
    variables: {
      tags: {
        value: {
          oneofKind: 'array',
          array: {
            items: [{ value: { oneofKind: 'str', str: 'alpha' } }, { value: { oneofKind: 'str', str: 'beta' } }],
          },
        },
      },
      scores: {
        value: {
          oneofKind: 'map',
          map: {
            entries: [
              { key: { value: { oneofKind: 'str', str: 'alice' } }, value: { value: { oneofKind: 'int', int: '10' } } },
              { key: { value: { oneofKind: 'str', str: 'bob' } }, value: { value: { oneofKind: 'int', int: '20' } } },
            ],
          },
        },
      },
    },
  })
  const finished = await waitForRun(client, run.id!)
  console.log(`example-arrays-and-maps -> ${LHStatus[finished.status]} (${run.id!.id})`)
  config.close()
  if (finished.status !== LHStatus.COMPLETED) process.exit(1)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
