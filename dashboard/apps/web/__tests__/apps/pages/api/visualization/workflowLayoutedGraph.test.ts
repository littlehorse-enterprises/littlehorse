import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/visualization/workflowLayoutedGraph'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import type { WfSpecId } from '../../../../../littlehorse-public-api/object_id'
import * as nextAuth from 'next-auth/next'
import LHClient from '../../../../../pages/api/LHClient'

jest.mock('../../../../../pages/api/LHClient')
jest.mock('../../../../../pages/api/grpcMethodCallHandler')
jest.mock( '../../../../../app/wfspec/[id]/[version]/components/visualizer/mappers/GraphLayouter')


describe('Layouted Graph API', () => {
    beforeEach(() => {
        (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => ({}))
    })

    it('should perform a grpc request to get the wfSpec sending the right request body', async () => {
        jest.spyOn(nextAuth, 'getServerSession').mockResolvedValue({ accessToken: 'ANY_ACCESS_TOKEN' })

        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.body = JSON.stringify({ wfSpecName: 'ANY_WF_SPEC', version: 0 })

        await handler(req, res)

        expect(grpcCallHandler.makeGrpcCall)
            .toHaveBeenCalledWith('getWfSpec',
                req, res, {
                    name: 'ANY_WF_SPEC',
                    version: 0
                } as WfSpecId)
    })

    it('should retrn a 401 error when no active session', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        jest.spyOn(nextAuth, 'getServerSession').mockResolvedValue(null)

        req.body = JSON.stringify({ wfSpecName: 'ANY_WF_SPEC', version: 0 })

        const statusMock = jest.fn()
        const jsonMock = jest.fn()

        res.status = statusMock.mockImplementation(() => ({
            json: jsonMock
        }))

        await handler(req, res)

        expect(jsonMock).toHaveBeenCalledWith({
            status: 401,
            message: 'You need to be authenticated to access this resource.'
        })
    })
})
