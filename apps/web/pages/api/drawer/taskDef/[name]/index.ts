import type { NextApiRequest, NextApiResponse } from 'next'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../../../../littlehorse-public-api/service'
import LHClient from '../../../LHClient'

export default async function handler(
  req: NextApiRequest,
  res: NextApiResponse
) {
  if (req.method === 'GET') {
    try {
      const client: Client<LHPublicApiDefinition> = LHClient.getInstance()

      const response = await client.getTaskDef({ name: req.query.name } as any)

      res.json({
        code: 'OK',
        data: { result: response }
      }) 
    } catch (error) {
      console.log('Error during GRPC call:', error)
      res.send({
        error: `Something went wrong.${error}`,
      }) 
    }
  }
}
