import type { NextApiRequest, NextApiResponse } from 'next'
import { makeGrpcCall } from '../grpcMethodCallHandler'
import type { WfSpecIdList } from '../../../littlehorse-public-api/service'
import { SearchWfSpecRequest } from '../../../littlehorse-public-api/service'
import type { WfSpecId } from '../../../littlehorse-public-api/object_id'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    if (req.method === 'POST') {
        const response = await makeGrpcCall('searchWfSpec', req, res, SearchWfSpecRequest.fromJSON(JSON.parse(req.body)))

        const resultsWithFormattedVersion = response.results.map((result: WfSpecId)  => ({
            ...result, version: `${result.majorVersion}.${result.revision}`
        }))

        res.json({ ...response, results: resultsWithFormattedVersion } as WfSpecIdList)
    }
}
