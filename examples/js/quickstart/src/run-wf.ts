import { LHConfig } from 'littlehorse-client'
import { VariableValue } from 'littlehorse-client/proto'

/**
 * Runs the quickstart workflow and then posts the correlated identity-verified event.
 *
 * Usage:
 *   npx tsx src/run-wf.ts [full-name] [email] [ssn]
 *
 * Defaults:
 *   full-name = "Obi-Wan Kenobi"
 *   email     = "obiwan@jedi.temple"
 *   ssn       = 123456789
 */
async function main() {
  const config = LHConfig.from({})
  const client = config.getClient()

  const fullName = process.argv[2] ?? 'Obi-Wan Kenobi'
  const email = process.argv[3] ?? 'obiwan@jedi.temple'
  const ssn = parseInt(process.argv[4] ?? '123456789', 10)

  const fullNameVar: VariableValue = { value: { $case: 'str', value: fullName } }
  const emailVar: VariableValue = { value: { $case: 'str', value: email } }
  const ssnVar: VariableValue = { value: { $case: 'int', value: ssn } }

  console.log(`Running quickstart workflow for "${fullName}" (${email})...`)

  const wfRun = await client.runWf({
    wfSpecName: 'quickstart',
    variables: {
      'full-name': fullNameVar,
      email: emailVar,
      ssn: ssnVar,
    },
  })

  const wfRunId = wfRun.id?.id
  console.log(`Started WfRun: ${wfRunId}`)
  console.log()
  console.log('Waiting 3 seconds for identity verification task to complete...')
  await new Promise(resolve => setTimeout(resolve, 3000))

  console.log(`Posting CorrelatedEvent "identity-verified" for ${email} (BOOL: true)...`)
  const eventContent: VariableValue = { value: { $case: 'bool', value: true } }
  await client.putCorrelatedEvent({
    key: email,
    externalEventDefId: { name: 'identity-verified' },
    content: eventContent,
  })

  console.log('CorrelatedEvent posted successfully!')
  console.log()
  console.log('Check the WfRun status with:')
  console.log(`  lhctl get wfRun ${wfRunId}`)
  console.log(`  lhctl list nodeRun ${wfRunId}`)
  console.log(`  lhctl list taskRun ${wfRunId}`)
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
