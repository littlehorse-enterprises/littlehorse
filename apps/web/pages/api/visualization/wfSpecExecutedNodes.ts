import type { NextApiRequest, NextApiResponse } from 'next'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../../littlehorse-public-api/service'
import LHClient from '../LHClient'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {

  if (req.method === 'POST') {
    //TODO: Insecure channel needs to be changed by a secure one
    const client: Client<LHPublicApiDefinition> = LHClient.getInstance()

    try {
      const parsedRequestBody = JSON.parse(req.body)

      const wfRun = await client.getWfRun({ id: parsedRequestBody.id } as any)

      const wfRunNodes: any[] = []
      const wfSpecNodesThatWereExecuted = {}

      const wfRunCurrentPosition = wfRun.threadRuns[0].currentNodePosition

      for (let position = 0; position <= wfRunCurrentPosition; position++) {
        const nodeRun = await client.getNodeRun({ wfRunId: wfRun.id, threadRunNumber: wfRun.threadRuns[0].number, position } as any)
        wfRunNodes.push(nodeRun)
      }

      const wfSpec = await client.getWfSpec({ name: wfRun.wfSpecName, version: wfRun.wfSpecVersion } as any)

      wfRunNodes.forEach((runNode, index) => {
        const nodeName = runNode.nodeName
        wfSpecNodesThatWereExecuted[nodeName] = wfSpec.threadSpecs.entrypoint.nodes[runNode.nodeName]
        wfSpecNodesThatWereExecuted[nodeName].newPosition = runNode.position
                
        // wfSpecNodesThatWereExecuted.push(wfSpec.threadSpecs['entrypoint']['nodes'][runNode.nodeName]);
      })

      // console.log('server side')

      // console.log('wfRunNodes', wfRunNodes)
      // console.log('executedWfSpecNodes', wfSpecNodesThatWereExecuted)

      const response = {
        name: wfSpec.name,
        version: wfSpec.version,
        status: wfSpec.status,
        threadSpecs: wfSpec.threadSpecs,
        entrypointThreadName: wfSpec.entrypointThreadName
      }

      response.threadSpecs.entrypoint.nodes = wfSpecNodesThatWereExecuted as any

      res.send(response) 

    } catch (error) {
      console.log(`wfSpecExecutedNodes.ts - Error during GRPC call:`, error)
      res.send({
        error: `Something went wrong.${error}`,
      }) 
    }
  }
}
