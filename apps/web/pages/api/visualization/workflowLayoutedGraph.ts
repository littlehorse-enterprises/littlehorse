import type { NextApiRequest, NextApiResponse } from 'next'
import ElkConstructor from 'elkjs/lib/elk.bundled.js'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../../littlehorse-public-api/service'
import type {
    ReactFlowGraph
} from '../../../app/wfspec/[id]/[version]/components/visualizer/mappers/GraphLayouter'
import GraphLayouter from '../../../app/wfspec/[id]/[version]/components/visualizer/mappers/GraphLayouter'
import EdgeLabelExtractor from '../../../app/wfspec/[id]/[version]/components/visualizer/extractors/EdgeLabelExtractor'
import type { WfSpec } from '../../../littlehorse-public-api/wf_spec'
import LHClient from '../LHClient'


export default async function handler(req: NextApiRequest, res: NextApiResponse) {

    if (req.method === 'POST') {
    //TODO: Insecure channel needs to be changed by a secure one
        const client: Client<LHPublicApiDefinition> = LHClient.getInstance()

        try {
            const parsedRequestBody = JSON.parse(req.body)

            const wfSpec: WfSpec = await client.getWfSpec({
                name: parsedRequestBody.wfSpecName,
                version: parsedRequestBody.version
            } as any)

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
        } catch (error) {
            console.error('WfSpecLayoutedGraph - Error during GRPC call:', error)
            res.status(404)
            res.send({
                error: `Something went wrong.${error}`,
            })
        }
    }
}
