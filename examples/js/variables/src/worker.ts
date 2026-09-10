import { LHTaskWorker, Workflow, createTaskWorker, lhMasked } from 'littlehorse-client'
import { VariableType } from 'littlehorse-client/proto'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()

type ProcessedText = {
  text: string
  sentimentScore: number
  addLength: boolean
  userId: number
}

function sentimentAnalysis(text: string): number {
  console.log(`Executing task sentiment-analysis vars (${text})`)
  return Math.random() * 100.0
}

function processText(text: string, sentimentScore: number, addLength: boolean, userId: number): ProcessedText {
  console.log(`Executing task process-text vars (${text}, ${sentimentScore}, ${addLength}, ${userId})`)
  return { text, sentimentScore, addLength, userId }
}

function send(processedText: ProcessedText): string {
  console.log(`Executing task send vars (${JSON.stringify(processedText)})`)
  return ''
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const analyzer = createTaskWorker(sentimentAnalysis, 'sentiment-analysis', config, {
    inputVars: { text: lhMasked(z.string()) },
    outputSchema: z.number(),
  })
  const processor = createTaskWorker(processText, 'process-text', config, {
    inputVars: {
      text: lhMasked(z.string()),
      sentimentScore: z.number(),
      addLength: z.boolean(),
      userId: z.number().int(),
    },
    outputSchema: lhMasked(
      z.object({
        text: z.string(),
        sentimentScore: z.number(),
        addLength: z.boolean(),
        userId: z.number().int(),
      })
    ),
  })
  const sender = createTaskWorker(send, 'send', config, {
    inputVars: {
      processedText: lhMasked(
        z.object({
          text: z.string(),
          sentimentScore: z.number(),
          addLength: z.boolean(),
          userId: z.number().int(),
        })
      ),
    },
    outputSchema: z.string(),
  })
  for (const w of [analyzer, processor, sender]) await ensureTaskDef(w)

  const wf = Workflow.newWorkflow('example-variables', thread => {
    // Optional settings go in an options object; the chained style
    // (.searchable() etc.) works too.
    const inputText = thread.declareStr('input-text', {
      searchable: true,
      masked: true,
    })

    const addLength = thread.declareBool('add-length', { searchable: true })

    const userId = thread.declareInt('user-id', { searchable: true })

    const sentimentScore = thread.declareDouble('sentiment-score', {
      searchable: true,
    })

    const processedResult = thread.declareJsonObj('processed-result', {
      searchableOn: [{ fieldPath: '$.sentimentScore', fieldType: VariableType.DOUBLE }],
      masked: true,
    })

    const sentimentAnalysisOutput = thread.execute('sentiment-analysis', inputText)
    sentimentScore.assign(sentimentAnalysisOutput)
    const processedTextOutput = thread.execute('process-text', inputText, sentimentScore, addLength, userId)
    processedResult.assign(processedTextOutput)
    thread.execute('send', processedResult)
  })
  await wf.registerWfSpec(config)

  for (const w of [analyzer, processor, sender]) await w.start()
  console.log('ready: polling for sentiment-analysis, process-text, send tasks')
  console.log(
    `run the workflow:  lhctl run example-variables input-text 'this is a very long text' add-length false user-id 1234`
  )

  const shutdown = async () => {
    await Promise.all([analyzer.close(), processor.close(), sender.close()])
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
