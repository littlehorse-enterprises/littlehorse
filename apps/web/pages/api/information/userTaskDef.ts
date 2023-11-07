import type { NextApiRequest, NextApiResponse } from 'next'
import { createClient } from 'nice-grpc/lib/client/ClientFactory'
import { createChannel } from 'nice-grpc/lib/client/channel'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../../littlehorse-public-api/service'
import LHClient from '../LHClient'

export default async function handler(
  req: NextApiRequest,
  res: NextApiResponse
) {
  if (req.method === 'POST') {
    const body = JSON.parse(req.body)
    const { id, version } = body

    try {
      const client: Client<LHPublicApiDefinition> = LHClient.getInstance()

      const response = await client.getUserTaskDef({ name: id, version } as any)

      res.send({ result: response }) 
    } catch (error) {
      console.log('Error during GRPC call:', error)
      res.send({
        error: `Something went wrong.${error}`,
      }) 
    }
  }
}
