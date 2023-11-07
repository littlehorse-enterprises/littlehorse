import type { NextApiRequest, NextApiResponse } from 'next'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../../littlehorse-public-api/service'
import { SearchWfRunRequest } from '../../../littlehorse-public-api/service'
import LHClient from '../LHClient'

export default async function handler(req:NextApiRequest, res:NextApiResponse) {
  if(req.method === 'POST'){
    //TODO: Insecure channel needs to be changed by a secure one
    const client: Client<LHPublicApiDefinition> = LHClient.getInstance()
      
    try {
      const request = SearchWfRunRequest.fromJSON(JSON.parse(req.body))
      const response = await client.searchWfRun(request as any)
      res.send(response) 
    } catch (error) {
      console.log('Error during GRPC call:', error)
      res.send({
        error: `Something went wrong.${error}`
      }) 
    }
  }
}
