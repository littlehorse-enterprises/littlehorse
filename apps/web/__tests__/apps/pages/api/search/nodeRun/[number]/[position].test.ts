import type { RequestMethod } from 'node-mocks-http'
import { createMocks } from 'node-mocks-http'

import type { NextApiRequest, NextApiResponse } from 'next'
import handler from '../../../../../../../pages/api/search/nodeRun/[number]/[position]'

function mockRequestResponse (method: RequestMethod='GET') {
    const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method })

    req.headers = {
        'Content-Type': 'application/json'
    }

    req.body = {}

    return { req, res }
}

describe('nodeRun API', () => {
    it('should reject request for unauthenticated users', async () => {
        const serverSessionForUnAuthenticatedUser = null
        jest.mock('next-auth/next', () => {
            const originalModule = jest.requireActual('next-auth/next')
            return {
                __esModule: true,
                ...originalModule,
                getServerSession: serverSessionForUnAuthenticatedUser
            }
        })

        const { req, res } = mockRequestResponse('POST')

        await handler(req, res)

        expect(res.statusCode).toEqual(401)
    })
})