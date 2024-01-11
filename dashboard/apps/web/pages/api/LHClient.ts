import type { Channel } from 'nice-grpc'
import { ChannelCredentials, createChannel, createClientFactory } from 'nice-grpc'
import type { Client } from 'nice-grpc/src/client/Client'
import { LittleHorseDefinition } from '../../littlehorse-public-api/service'
import { Metadata } from 'nice-grpc-common'
import * as fs from 'fs'

export default class LHClient {
    private static channel: Channel
    // eslint-disable-next-line @typescript-eslint/no-empty-function -- needed for a singleton
    private constructor() {
    }

    public static getInstance(accessToken?: string): Client<LittleHorseDefinition> {
        if (process.env.API_URL === undefined) {
            throw new Error('Not able to get the API URL from your configuration.')
        }

        if (process.env.LHD_OAUTH_ENABLED === 'true') {
            this.createUniqueSecureChannel(process.env.API_URL)

            return createClientFactory().use((call, options) =>
                call.next(call.request, {
                    ...options,
                    metadata: Metadata(options.metadata).set(
                        'Authorization',
                        `Bearer ${accessToken}`,
                    ),
                })).create(LittleHorseDefinition, LHClient.channel)
        } else {
            this.createUniqueInsecureChannel(process.env.API_URL)

            return createClientFactory().use((call, options) =>
                call.next(call.request, {
                    ...options,
                })).create(LittleHorseDefinition, LHClient.channel)
        }
    }

    private static createUniqueSecureChannel(apiUrl: string) {
        const caCertificatePath: string | undefined = process.env.LHC_CA_CERT
        const applicationHasProvidedCACertificate = caCertificatePath !== undefined

        if (LHClient.channel === undefined) {
            if (applicationHasProvidedCACertificate) {
                LHClient.channel = createChannel(apiUrl,
                    ChannelCredentials.createSsl(fs.readFileSync(caCertificatePath)))
            } else {
                LHClient.channel = createChannel(apiUrl,
                    ChannelCredentials.createSsl())
            }
        }
    }

    private static createUniqueInsecureChannel(apiUrl: string) {
        if (LHClient.channel === undefined) {
            LHClient.channel = createChannel(apiUrl)
        }
    }
}
