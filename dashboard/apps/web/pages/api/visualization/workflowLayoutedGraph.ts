import type { NextApiRequest, NextApiResponse } from 'next'
import ElkConstructor from 'elkjs/lib/elk.bundled.js'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LittleHorseDefinition } from '../../../littlehorse-public-api/service'
import type { ReactFlowGraph } from '../../../app/wfspec/[id]/[version]/components/visualizer/mappers/GraphLayouter'
import GraphLayouter from '../../../app/wfspec/[id]/[version]/components/visualizer/mappers/GraphLayouter'
import EdgeLabelExtractor from '../../../app/wfspec/[id]/[version]/components/visualizer/extractors/EdgeLabelExtractor'
import type { WfSpec } from '../../../littlehorse-public-api/wf_spec'
import LHClient from '../LHClient'
import { getServerSession } from 'next-auth/next'
import type { SessionWithJWTExpireTime } from '../auth/[...nextauth]'
import { authOptions } from '../auth/[...nextauth]'
import { makeGrpcCall } from '../grpcMethodCallHandler'
import { WfSpecId } from '../../../littlehorse-public-api/object_id'
import { constants } from 'http2'

const unauthorizedResponseContent = {
    status: constants.HTTP_STATUS_UNAUTHORIZED,
    message: 'You need to be authenticated to access this resource.'
}

export default async function handler(req: NextApiRequest, res: NextApiResponse) {

    if (req.method === 'POST') {
        let session: SessionWithJWTExpireTime | null
        let client: Client<LittleHorseDefinition>

        if (__AUTHENTICATION_ENABLED__) {
            session = await getServerSession(req, res, authOptions)
            if (!session) {
                res.status(constants.HTTP_STATUS_UNAUTHORIZED)
                    .json(unauthorizedResponseContent)
                return
            }
            client = LHClient.getInstance(session.accessToken)
        } else {
            client = LHClient.getInstance()
        }

        const parsedRequestBody = JSON.parse(req.body)

        const wfSpec: WfSpec = await makeGrpcCall('getWfSpec', req, res, WfSpecId.fromJSON({
            name: parsedRequestBody.wfSpecName,
            majorVersion: parsedRequestBody.majorVersion,
            revision: parsedRequestBody.revision
        }))

        const elkInstance = new ElkConstructor()
        let layoutedGraph: ReactFlowGraph

        if (parsedRequestBody.isWfSpecVisualization) {
            layoutedGraph = await new GraphLayouter(
                elkInstance,
                EdgeLabelExtractor.extract).getLayoutedGraph(wfSpec, parsedRequestBody.wfSpecName, parsedRequestBody.threadSpec)
        } else {
            layoutedGraph = await new GraphLayouter(
                elkInstance,
                EdgeLabelExtractor.extract,
                client).getLayoutedGraphForWfRun(wfSpec,
                parsedRequestBody.wfSpecName,
                parsedRequestBody.wfRunId,
                parsedRequestBody.threadRunNumber,
                parsedRequestBody.threadSpec
            )
        }

        res.send(layoutedGraph)
    }
}
