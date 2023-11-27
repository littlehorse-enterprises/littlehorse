import { makeGrpcCall, handleGrpcCallWithNext } from '../../../../pages/api/grpcMethodCallHandler'
import type { NextApiRequest, NextApiResponse } from 'next'
import LHClient from '../../../../pages/api/LHClient'
import * as nextAuth from 'next-auth/next'
import type { SearchWfSpecRequest, WfSpecIdList } from '../../../../littlehorse-public-api/service'
import { ClientError, Status } from 'nice-grpc-common'
import { constants } from 'http2'

jest.mock('../../../../pages/api/LHClient')

describe('AUTHENTICATION ENABLED', () => {
    beforeEach(() => {
        __AUTHENTICATION_ENABLED__ = true
    })
    
    describe('Grpc method call handler that returns the success response', () => {
        it('a result is returned by the invoked grpc method when the a session is active', async () => {        
            jest.spyOn(nextAuth, 'getServerSession').mockResolvedValue({ accessToken: 'ANY_ACCESS_TOKEN' })
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const response: NextApiResponse = {
                send: jest.fn(),
            } as unknown as NextApiResponse
    
            (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => ({
                searchWfSpec: (_: SearchWfSpecRequest): Promise<WfSpecIdList> => {
                    return Promise.resolve({
                        results: [ { name: 'ANY_SPEC', version: 0 } ]
                    } as WfSpecIdList)
                }
            }))
    
            const wfRun = await makeGrpcCall('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(wfRun).toEqual({
                results: [ { name: 'ANY_SPEC', version: 0 } ]
            })
        })
    
        it('should call grpc with the provided request body', async () => {
            jest.spyOn(nextAuth, 'getServerSession').mockResolvedValue({ accessToken: 'ANY_ACCESS_TOKEN' })
    
            const searchWfSpecMock = jest.fn()
    
            const request: NextApiRequest = {
                body: JSON.stringify({
                    id: 'ANY_WFRUN_ID',
                    number: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => {
                return ({
                    searchWfSpec: searchWfSpecMock
                })
            })
            const sendMock = jest.fn()
    
            const response: NextApiResponse = {
                send: sendMock,
            } as unknown as NextApiResponse
    
            const grpcRequestBody = {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            }
    
            await makeGrpcCall('searchWfSpec', request, response, grpcRequestBody)
    
            expect(searchWfSpecMock).toHaveBeenCalledWith(grpcRequestBody)
        })
    
        it('unauthorized response when grpc method answers with PERMISSION DENIED', async () => {
            jest.spyOn(nextAuth, 'getServerSession').mockResolvedValue({ accessToken: 'EXPIRED_ACCESS_TOKEN' })
    
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const statusMock = jest.fn()
            const jsonMock = jest.fn()
            const response: NextApiResponse = {
                send: jest.fn(),
                status: statusMock.mockImplementation(() => ({
                    json: jsonMock
                })),
            } as unknown as NextApiResponse
    
            (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => ({
                searchWfSpec: (_: SearchWfSpecRequest): Promise<WfSpecIdList> => {
                    return Promise.reject(new ClientError('/searchWfSpec', Status.PERMISSION_DENIED, 'Token is not active'))
                }
            }))
    
            await makeGrpcCall('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(statusMock).toHaveBeenCalledWith(constants.HTTP_STATUS_UNAUTHORIZED)
            expect(jsonMock).toHaveBeenCalledWith({
                status: 401,
                message: 'You need to be authenticated to access this resource.'
            })
        })
    
        it('unauthorized response when grpc method answers with UNAUTHENTICATED', async () => {
            jest.spyOn(nextAuth, 'getServerSession').mockResolvedValue({ accessToken: undefined })
    
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const statusMock = jest.fn()
            const jsonMock = jest.fn()
            const response: NextApiResponse = {
                send: jest.fn(),
                status: statusMock.mockImplementation(() => ({
                    json: jsonMock
                })),
            } as unknown as NextApiResponse
    
            (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => ({
                searchWfSpec: (_: SearchWfSpecRequest): Promise<WfSpecIdList> => {
                    return Promise.reject(new ClientError('/searchWfSpec', Status.UNAUTHENTICATED, 'Unauthenticated'))
                }
            }))
    
            await makeGrpcCall('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(statusMock).toHaveBeenCalledWith(constants.HTTP_STATUS_UNAUTHORIZED)
            expect(jsonMock).toHaveBeenCalledWith({
                status: 401,
                message: 'You need to be authenticated to access this resource.'
            })
        })
    
        it.each([
            [ Status.CANCELLED ],
            [ Status.UNKNOWN ],
            [ Status.INVALID_ARGUMENT ],
            [ Status.DEADLINE_EXCEEDED ],
            [ Status.NOT_FOUND ],
            [ Status.ALREADY_EXISTS ],
            [ Status.RESOURCE_EXHAUSTED ],
            [ Status.FAILED_PRECONDITION ],
            [ Status.ABORTED ],
            [ Status.OUT_OF_RANGE ],
            [ Status.UNIMPLEMENTED ],
            [ Status.INTERNAL ],
            [ Status.UNAVAILABLE ],
            [ Status.DATA_LOSS ],
        ])('internal server error response when grpc method answers with any error code but PERMISSION_DENIED or AUTHENTICATED', async (grpcStatusCode: Status) => {
            jest.spyOn(nextAuth, 'getServerSession').mockResolvedValue({ accessToken: 'A_VALID_TOKEN' })
    
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const statusMock = jest.fn()
            const jsonMock = jest.fn()
            const response: NextApiResponse = {
                send: jest.fn(),
                status: statusMock.mockImplementation(() => ({
                    json: jsonMock
                })),
            } as unknown as NextApiResponse
    
            (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => ({
                searchWfSpec: (_: SearchWfSpecRequest): Promise<WfSpecIdList> => {
                    return Promise.reject(new ClientError('/searchWfSpec', grpcStatusCode, 'Error Details.'))
                }
            }))
    
            await makeGrpcCall('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(statusMock).toHaveBeenCalledWith(constants.HTTP_STATUS_INTERNAL_SERVER_ERROR)
            expect(jsonMock).toHaveBeenCalledWith({
                status: 500,
                message: 'There was an error while processing your request. Error Details.'
            })
        })
    
        it('non authorized response when the is no active session', async () => {
            const nullSession = null
            jest.spyOn(nextAuth, 'getServerSession').mockResolvedValue(nullSession)
    
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const responseStatusMock = jest.fn()
            const response: NextApiResponse = {
                status: responseStatusMock.mockImplementation(() => ({
                    json: jest.fn()
                })),
            } as unknown as NextApiResponse
    
    
            await makeGrpcCall('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(responseStatusMock).toHaveBeenCalledWith(401)
        })
    
        it('non authorized response should include a json body with details about the error', async () => {
            const nullSession = null
            jest.spyOn(nextAuth, 'getServerSession').mockResolvedValue(nullSession)
    
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const jsonResponseMock = jest.fn()
            const response: NextApiResponse = {
                status: jest.fn().mockImplementation(() => ({
                    json: jsonResponseMock
                })),
            } as unknown as NextApiResponse
    
    
            await makeGrpcCall('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(jsonResponseMock).toHaveBeenCalledWith({
                status: 401,
                message: 'You need to be authenticated to access this resource.'
            })
        })
    
        it('should return the API Response', async () => {
            jest.spyOn(nextAuth, 'getServerSession').mockResolvedValue({ accessToken: 'ANY_ACCESS_TOKEN' })
    
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const sendMock = jest.fn()
            const response: NextApiResponse = {
                send: sendMock,
            } as unknown as NextApiResponse
    
            (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => ({
                searchWfSpec: (_: SearchWfSpecRequest): Promise<WfSpecIdList> => {
                    return Promise.resolve({
                        results: [ { name: 'ANY_SPEC', version: 0 } ]
                    } as WfSpecIdList)
                }
            }))
    
            const wfRunResponse = await makeGrpcCall('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(wfRunResponse).toEqual({
                results: [ { name: 'ANY_SPEC', version: 0 } ]
            })
        })
    })
    describe('Grpc Method Call handler that sends the success response through Next', () => {
        afterEach(() => {
            jest.resetAllMocks()
        })
    
        it('response should include the result returned by the invoked grpc method when the a session is active', async () => {
            jest.spyOn(nextAuth, 'getServerSession').mockResolvedValue({ accessToken: 'ANY_ACCESS_TOKEN' })
    
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const sendMock = jest.fn()
            const response: NextApiResponse = {
                send: sendMock,
            } as unknown as NextApiResponse
    
            (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => ({
                searchWfSpec: (_: SearchWfSpecRequest): Promise<WfSpecIdList> => {
                    return Promise.resolve({
                        results: [ { name: 'ANY_SPEC', version: 0 } ]
                    } as WfSpecIdList)
                }
            }))
    
            await handleGrpcCallWithNext('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(sendMock).toHaveBeenCalledWith({
                results: [ { name: 'ANY_SPEC', version: 0 } ]
            })
        })
    
        it('should call grpc with the provided request body', async () => {
            jest.spyOn(nextAuth, 'getServerSession').mockResolvedValue({ accessToken: 'ANY_ACCESS_TOKEN' })
    
            const searchWfSpecMock = jest.fn()
    
            const request: NextApiRequest = {
                body: JSON.stringify({
                    id: 'ANY_WFRUN_ID',
                    number: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => {
                return ({
                    searchWfSpec: searchWfSpecMock
                })
            })
            const sendMock = jest.fn()
    
            const response: NextApiResponse = {
                send: sendMock,
            } as unknown as NextApiResponse
    
            const grpcRequestBody = {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            }
    
            await handleGrpcCallWithNext('searchWfSpec', request, response, grpcRequestBody)
    
            expect(searchWfSpecMock).toHaveBeenCalledWith(grpcRequestBody)
        })
    
        it('unauthorized response when grpc method answers with PERMISSION DENIED', async () => {
            jest.spyOn(nextAuth, 'getServerSession').mockResolvedValue({ accessToken: 'EXPIRED_ACCESS_TOKEN' })
    
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const statusMock = jest.fn()
            const jsonMock = jest.fn()
            const response: NextApiResponse = {
                send: jest.fn(),
                status: statusMock.mockImplementation(() => ({
                    json: jsonMock
                })),
            } as unknown as NextApiResponse
    
            (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => ({
                searchWfSpec: (_: SearchWfSpecRequest): Promise<WfSpecIdList> => {
                    return Promise.reject(new ClientError('/searchWfSpec', Status.PERMISSION_DENIED, 'Token is not active'))
                }
            }))
    
            await handleGrpcCallWithNext('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(statusMock).toHaveBeenCalledWith(constants.HTTP_STATUS_UNAUTHORIZED)
            expect(jsonMock).toHaveBeenCalledWith({
                status: 401,
                message: 'You need to be authenticated to access this resource.'
            })
        })
    
        it('unauthorized response when grpc method answers with UNAUTHENTICATED', async () => {
            jest.spyOn(nextAuth, 'getServerSession').mockResolvedValue({ accessToken: undefined })
    
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const statusMock = jest.fn()
            const jsonMock = jest.fn()
            const response: NextApiResponse = {
                send: jest.fn(),
                status: statusMock.mockImplementation(() => ({
                    json: jsonMock
                })),
            } as unknown as NextApiResponse
    
            (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => ({
                searchWfSpec: (_: SearchWfSpecRequest): Promise<WfSpecIdList> => {
                    return Promise.reject(new ClientError('/searchWfSpec', Status.UNAUTHENTICATED, 'Unauthenticated'))
                }
            }))
    
            await handleGrpcCallWithNext('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(statusMock).toHaveBeenCalledWith(constants.HTTP_STATUS_UNAUTHORIZED)
            expect(jsonMock).toHaveBeenCalledWith({
                status: 401,
                message: 'You need to be authenticated to access this resource.'
            })
        })
    
        it.each([
            [ Status.CANCELLED ],
            [ Status.UNKNOWN ],
            [ Status.INVALID_ARGUMENT ],
            [ Status.DEADLINE_EXCEEDED ],
            [ Status.NOT_FOUND ],
            [ Status.ALREADY_EXISTS ],
            [ Status.RESOURCE_EXHAUSTED ],
            [ Status.FAILED_PRECONDITION ],
            [ Status.ABORTED ],
            [ Status.OUT_OF_RANGE ],
            [ Status.UNIMPLEMENTED ],
            [ Status.INTERNAL ],
            [ Status.UNAVAILABLE ],
            [ Status.DATA_LOSS ],
        ])('internal server error response when grpc method answers with any error code but PERMISSION_DENIED', async (grpcStatusCode: Status) => {
            jest.spyOn(nextAuth, 'getServerSession').mockResolvedValue({ accessToken: 'A_VALID_TOKEN' })
    
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const statusMock = jest.fn()
            const jsonMock = jest.fn()
            const response: NextApiResponse = {
                send: jest.fn(),
                status: statusMock.mockImplementation(() => ({
                    json: jsonMock
                })),
            } as unknown as NextApiResponse
    
            (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => ({
                searchWfSpec: (_: SearchWfSpecRequest): Promise<WfSpecIdList> => {
                    return Promise.reject(new ClientError('/searchWfSpec', grpcStatusCode, 'Error Details.'))
                }
            }))
    
            await handleGrpcCallWithNext('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(statusMock).toHaveBeenCalledWith(constants.HTTP_STATUS_INTERNAL_SERVER_ERROR)
            expect(jsonMock).toHaveBeenCalledWith({
                status: 500,
                message: 'There was an error while processing your request. Error Details.'
            })
        })
    
        it('non authorized response when the is no active session', async () => {
            const nullSession = null
            jest.spyOn(nextAuth, 'getServerSession').mockResolvedValue(nullSession)
    
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const responseStatusMock = jest.fn()
            const response: NextApiResponse = {
                status: responseStatusMock.mockImplementation(() => ({
                    json: jest.fn()
                })),
            } as unknown as NextApiResponse
    
    
            await handleGrpcCallWithNext('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(responseStatusMock).toHaveBeenCalledWith(401)
        })
    
        it('non authorized response should include a json body with details about the error', async () => {
            const nullSession = null
            jest.spyOn(nextAuth, 'getServerSession').mockResolvedValue(nullSession)
    
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const jsonResponseMock = jest.fn()
            const response: NextApiResponse = {
                status: jest.fn().mockImplementation(() => ({
                    json: jsonResponseMock
                })),
            } as unknown as NextApiResponse
    
    
            await handleGrpcCallWithNext('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(jsonResponseMock).toHaveBeenCalledWith({
                status: 401,
                message: 'You need to be authenticated to access this resource.'
            })
        })
    })
})


describe('AUTHENTICATION DISABLED', () => {
    beforeEach(() => {
        __AUTHENTICATION_ENABLED__ = false
    })
    describe('Grpc method call handler that returns the success response', () => {
        it('a result is returned by the invoked grpc method', async () => {        
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const response: NextApiResponse = {
                send: jest.fn(),
            } as unknown as NextApiResponse
    
            (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => ({
                searchWfSpec: (_: SearchWfSpecRequest): Promise<WfSpecIdList> => {
                    return Promise.resolve({
                        results: [ { name: 'ANY_SPEC', version: 0 } ]
                    } as WfSpecIdList)
                }
            }))
    
            const wfRun = await makeGrpcCall('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(wfRun).toEqual({
                results: [ { name: 'ANY_SPEC', version: 0 } ]
            })
        })
    
        it('should call grpc with the provided request body', async () => {
            const searchWfSpecMock = jest.fn()
    
            const request: NextApiRequest = {
                body: JSON.stringify({
                    id: 'ANY_WFRUN_ID',
                    number: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => {
                return ({
                    searchWfSpec: searchWfSpecMock
                })
            })
            const sendMock = jest.fn()
    
            const response: NextApiResponse = {
                send: sendMock,
            } as unknown as NextApiResponse
    
            const grpcRequestBody = {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            }
    
            await makeGrpcCall('searchWfSpec', request, response, grpcRequestBody)
    
            expect(searchWfSpecMock).toHaveBeenCalledWith(grpcRequestBody)
        })
    
        it.each([
            [ Status.CANCELLED ],
            [ Status.UNKNOWN ],
            [ Status.INVALID_ARGUMENT ],
            [ Status.DEADLINE_EXCEEDED ],
            [ Status.NOT_FOUND ],
            [ Status.ALREADY_EXISTS ],
            [ Status.RESOURCE_EXHAUSTED ],
            [ Status.FAILED_PRECONDITION ],
            [ Status.ABORTED ],
            [ Status.OUT_OF_RANGE ],
            [ Status.UNIMPLEMENTED ],
            [ Status.INTERNAL ],
            [ Status.UNAVAILABLE ],
            [ Status.DATA_LOSS ],
        ])('internal server error response when grpc method answers with any error code but PERMISSION_DENIED OR UNAUTHENTICATED', async (grpcStatusCode: Status) => {
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const statusMock = jest.fn()
            const jsonMock = jest.fn()
            const response: NextApiResponse = {
                send: jest.fn(),
                status: statusMock.mockImplementation(() => ({
                    json: jsonMock
                })),
            } as unknown as NextApiResponse
    
            (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => ({
                searchWfSpec: (_: SearchWfSpecRequest): Promise<WfSpecIdList> => {
                    return Promise.reject(new ClientError('/searchWfSpec', grpcStatusCode, 'Error Details.'))
                }
            }))
    
            await makeGrpcCall('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(statusMock).toHaveBeenCalledWith(constants.HTTP_STATUS_INTERNAL_SERVER_ERROR)
            expect(jsonMock).toHaveBeenCalledWith({
                status: 500,
                message: 'There was an error while processing your request. Error Details.'
            })
        })
    
        it('should return the API Response', async () => {
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const sendMock = jest.fn()
            const response: NextApiResponse = {
                send: sendMock,
            } as unknown as NextApiResponse
    
            (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => ({
                searchWfSpec: (_: SearchWfSpecRequest): Promise<WfSpecIdList> => {
                    return Promise.resolve({
                        results: [ { name: 'ANY_SPEC', version: 0 } ]
                    } as WfSpecIdList)
                }
            }))
    
            const wfRunResponse = await makeGrpcCall('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(wfRunResponse).toEqual({
                results: [ { name: 'ANY_SPEC', version: 0 } ]
            })
        })
    })
    describe('Grpc Method Call handler that sends the success response through Next', () => {
        afterEach(() => {
            jest.resetAllMocks()
        })
    
        it('response should include the result returned by the invoked grpc method when the a session is active', async () => {
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const sendMock = jest.fn()
            const response: NextApiResponse = {
                send: sendMock,
            } as unknown as NextApiResponse
    
            (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => ({
                searchWfSpec: (_: SearchWfSpecRequest): Promise<WfSpecIdList> => {
                    return Promise.resolve({
                        results: [ { name: 'ANY_SPEC', version: 0 } ]
                    } as WfSpecIdList)
                }
            }))
    
            await handleGrpcCallWithNext('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(sendMock).toHaveBeenCalledWith({
                results: [ { name: 'ANY_SPEC', version: 0 } ]
            })
        })
    
        it.each([
            [ Status.CANCELLED ],
            [ Status.UNKNOWN ],
            [ Status.INVALID_ARGUMENT ],
            [ Status.DEADLINE_EXCEEDED ],
            [ Status.NOT_FOUND ],
            [ Status.ALREADY_EXISTS ],
            [ Status.RESOURCE_EXHAUSTED ],
            [ Status.FAILED_PRECONDITION ],
            [ Status.ABORTED ],
            [ Status.OUT_OF_RANGE ],
            [ Status.UNIMPLEMENTED ],
            [ Status.INTERNAL ],
            [ Status.UNAVAILABLE ],
            [ Status.DATA_LOSS ],
        ])('internal server error response when grpc method answers with any error code but PERMISSION_DENIED or AUTHENTICATED', async (grpcStatusCode: Status) => {
            const request: NextApiRequest = {
                body: JSON.stringify({
                    wfRunId: 'ANY_WFRUN_ID',
                    threadNumber: 0,
                    position: 0
                }),
            } as NextApiRequest
    
            const statusMock = jest.fn()
            const jsonMock = jest.fn()
            const response: NextApiResponse = {
                send: jest.fn(),
                status: statusMock.mockImplementation(() => ({
                    json: jsonMock
                })),
            } as unknown as NextApiResponse
    
            (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => ({
                searchWfSpec: (_: SearchWfSpecRequest): Promise<WfSpecIdList> => {
                    return Promise.reject(new ClientError('/searchWfSpec', grpcStatusCode, 'Error Details.'))
                }
            }))
    
            await handleGrpcCallWithNext('searchWfSpec', request, response, {
                wfRunId: 'ANY_WFRUN_ID',
                threadNumber: 0,
                position: 0
            })
    
            expect(statusMock).toHaveBeenCalledWith(constants.HTTP_STATUS_INTERNAL_SERVER_ERROR)
            expect(jsonMock).toHaveBeenCalledWith({
                status: 500,
                message: 'There was an error while processing your request. Error Details.'
            })
        })
    })
})
