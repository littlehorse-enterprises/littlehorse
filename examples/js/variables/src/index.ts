import { LHConfig, Workflow, createTaskWorker, WorkerContext } from 'littlehorse-client'
import { z } from 'zod'

const ProcessedText = z.object({
  text: z.string(),
  sentimentScore: z.number(),
  addLength: z.boolean(),
  userId: z.number().int(),
})

function variablesWorkflow() {
  return Workflow.newWorkflow('example-variables', thread => {
    const inputText = thread.declareStr('input-text')
    const addLength = thread.declareBool('add-length')
    const userId = thread.declareInt('user-id')
    const sentimentScore = thread.declareDouble('sentiment-score')
    const processedResult = thread.declareJsonObj('processed-result')

    const sentimentOut = thread.execute('sentiment-analysis', inputText)
    sentimentScore.assign(sentimentOut)

    const processOut = thread.execute('process-text', inputText, sentimentScore, addLength, userId)
    processedResult.assign(processOut)

    thread.execute('send', processedResult)
  })
}

async function sentimentAnalysis(text: string, ctx: WorkerContext): Promise<number> {
  ctx.log(`sentiment-analysis input length=${text.length}`)
  return Math.random() * 100
}

async function processText(
  text: string,
  sentimentScore: number,
  addLength: boolean,
  userId: number,
  ctx: WorkerContext
): Promise<z.infer<typeof ProcessedText>> {
  ctx.log('process-text')
  return { text, sentimentScore, addLength, userId }
}

async function send(processedText: z.infer<typeof ProcessedText>, ctx: WorkerContext): Promise<string> {
  ctx.log(`send: ${JSON.stringify(processedText)}`)
  console.log('[send]', processedText)
  return ''
}

async function main() {
  const config = LHConfig.from({})
  const client = config.getClient()

  const w1 = createTaskWorker(sentimentAnalysis, 'sentiment-analysis', config, {
    inputVars: { text: z.string() },
    outputSchema: z.number(),
  })
  const w2 = createTaskWorker(processText, 'process-text', config, {
    inputVars: {
      text: z.string(),
      sentimentScore: z.number(),
      addLength: z.boolean(),
      userId: z.number().int(),
    },
    outputSchema: ProcessedText,
  })
  const w3 = createTaskWorker(send, 'send', config, {
    inputVars: { processedText: ProcessedText },
  })

  const workers = [w1, w2, w3]
  for (const w of workers) {
    if (!(await w.doesTaskDefExist())) {
      await w.registerTaskDef()
    }
  }

  console.log('Registering WfSpec "example-variables"...')
  await Workflow.registerWfSpec(variablesWorkflow(), client)

  await Promise.all(workers.map(w => w.start()))

  process.on('SIGINT', async () => {
    console.log('\nShutting down...')
    await Promise.all(workers.map(w => w.close()))
    process.exit(0)
  })
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
