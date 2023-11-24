import type { NextApiRequest, NextApiResponse } from 'next'
import { getServerSession } from 'next-auth/next'
import { authOptions } from './auth/[...nextauth]'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../littlehorse-public-api/service'
import LHClient from './LHClient'
import { Status } from 'nice-grpc-common'
import { constants } from 'http2'

const unauthorizedResponseContent = {
    status: constants.HTTP_STATUS_UNAUTHORIZED,
    message: 'You need to be authenticated to access this resource.'
}
export const makeGrpcCall = async (grpcMethodToCall: string,
    req: NextApiRequest, res: NextApiResponse, grpcRequestBody: object): Promise<any> => {
    const session = await getServerSession(req, res, authOptions)

    if (!session) {
        res.status(401)
            .json(unauthorizedResponseContent)
    } else {
        const client: Client<LHPublicApiDefinition> = LHClient.getInstance(session.accessToken)

        try {
            return await client[grpcMethodToCall](grpcRequestBody)
        } catch (error) {
            console.error('grpcMethodCallHandler - Error during GRPC call:', error)
            _handleError(error, res)
        }
    }
    
}
export const handleGrpcCallWithNext = async (grpcMethodToCall: string,
    req: NextApiRequest, res: NextApiResponse, grpcRequestBody: object) => {

    const session = await getServerSession(req, res, authOptions)

    if (!session) {
        res.status(401)
            .json(unauthorizedResponseContent)
    } else {
        const client: Client<LHPublicApiDefinition> = LHClient.getInstance(session.accessToken)

        try {
            const response: any = await client[grpcMethodToCall](grpcRequestBody)
            res.send(response)
        } catch (error) {
            console.error('grpcMethodCallHandler - Error during GRPC call:', error)
            _handleError(error, res)
        }
    }
}

function _handleError(error, res: NextApiResponse) {
    if (error.code === Status.PERMISSION_DENIED || error.code === Status.UNAUTHENTICATED) {
        res.status(constants.HTTP_STATUS_UNAUTHORIZED)
            .json(unauthorizedResponseContent)
    } else {
        res.status(constants.HTTP_STATUS_INTERNAL_SERVER_ERROR)
            .json({
                status: constants.HTTP_STATUS_INTERNAL_SERVER_ERROR,
                message: `There was an error while processing your request. ${error.details}`
            })
    }
}
