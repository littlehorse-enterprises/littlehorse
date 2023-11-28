import type { NextApiRequest, NextApiResponse } from 'next'
import ElkConstructor from 'elkjs/lib/elk.bundled.js'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../../littlehorse-public-api/service'
import type { ReactFlowGraph } from '../../../app/wfspec/[id]/[version]/components/visualizer/mappers/GraphLayouter'
import GraphLayouter from '../../../app/wfspec/[id]/[version]/components/visualizer/mappers/GraphLayouter'
import EdgeLabelExtractor from '../../../app/wfspec/[id]/[version]/components/visualizer/extractors/EdgeLabelExtractor'
import type { WfSpec } from '../../../littlehorse-public-api/wf_spec'
import LHClient from '../LHClient'
import { getServerSession } from 'next-auth/next'
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
        const session = await getServerSession(req, res, authOptions)

        if (session) {
            const client: Client<LHPublicApiDefinition> = LHClient.getInstance(session.accessToken)
            const parsedRequestBody = JSON.parse(req.body)

            const wfSpec: WfSpec = await makeGrpcCall('getWfSpec', req, res, WfSpecId.fromJSON({
                name: parsedRequestBody.wfSpecName,
                majorVersion: parsedRequestBody.version,
                revision: 0 // TODO: OSS - bring this from the UI
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
        } else {
            res.status(constants.HTTP_STATUS_UNAUTHORIZED)
                .json(unauthorizedResponseContent)
        }
    }
}
