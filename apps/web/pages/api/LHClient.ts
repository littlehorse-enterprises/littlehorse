import type { Channel } from 'nice-grpc'
import { createChannel, createClient } from 'nice-grpc'
import type { Client } from 'nice-grpc/src/client/Client'
import { LHPublicApiDefinition } from '../../littlehorse-public-api/service'

export default class LHClient {
    private static client: Client<LHPublicApiDefinition>
    /*
   eslint-disable-next-line @typescript-eslint/no-empty-function
   */
    private constructor() {}
    public static getInstance(): Client<LHPublicApiDefinition> {
        if (process.env.API_URL === undefined) {
            throw new Error('Not able to get the API URL from your configuration.')
        }

        if (LHClient.client === undefined) {
            const channel: Channel = createChannel(process.env.API_URL)
            LHClient.client = createClient(LHPublicApiDefinition, channel)
        }

        return LHClient.client
    }
}
