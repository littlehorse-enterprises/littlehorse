import type { NextApiRequest, NextApiResponse } from 'next'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../../littlehorse-public-api/service'
import LHClient from '../LHClient'
import type { WfSpec } from '../../../littlehorse-public-api/wf_spec'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {

  if (req.method === 'POST') {
    //TODO: Insecure channel needs to be changed by a secure one
    const client: Client<LHPublicApiDefinition> = LHClient.getInstance()

    try {
      const parsedRequestBody = JSON.parse(req.body)
      const response: WfSpec = await client.getWfSpec({
        name: parsedRequestBody.id,
        version: parsedRequestBody.version
      } as any)
      res.send(response) 
    } catch (error) {
      console.log('WfSpec - Error during GRPC call:', error)
      res.send({
        error: `Something went wrong.${error}`,
      }) 
    }
  }
}
