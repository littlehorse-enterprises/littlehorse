import { faker } from '@faker-js/faker'
import { WaitForThreadsRun_WaitingThreadStatus, type NodeRun } from '../../../littlehorse-public-api/node_run'
import type { ThreadRun } from '../../../littlehorse-public-api/wf_run'
import { ThreadType } from '../../../littlehorse-public-api/wf_run'
import { LHStatus } from '../../../littlehorse-public-api/common_enums'
import ThreadRunsHandler from './ThreadRunsHandler'

describe('handle Thread Runs related logic', () => {
    it('should return all the thread runs that the node run is waiting for', () => {
        const threadRuns: ThreadRun[] = [
            {
                wfSpecId: {
                    name: 'A_WFSPEC',
                    majorVersion: 0,
                    revision: 0
                },
                'number': 0,
                'status': LHStatus.RUNNING,
                'threadSpecName': 'entrypoint',
                'startTime': '2023-10-30T19:40:51.912Z',
                'childThreadIds': [
                    1,
                    2
                ],
                'haltReasons': [],
                'currentNodePosition': 3,
                'handledFailedChildren': [],
                'type': ThreadType.ENTRYPOINT
            },
            {
                wfSpecId: {
                    name: 'A_WFSPEC',
                    majorVersion: 0,
                    revision: 0
                },
                'number': 1,
                'status': LHStatus.RUNNING,
                'threadSpecName': 'approval',
                'startTime': '2023-10-30T19:40:51.919Z',
                'childThreadIds': [],
                'parentThreadId': 0,
                'haltReasons': [],
                'currentNodePosition': 2,
                'handledFailedChildren': [],
                'type': ThreadType.CHILD
            },
            {
                wfSpecId: {
                    name: 'A_WFSPEC',
                    majorVersion: 0,
                    revision: 0
                },
                'number': 2,
                'status': LHStatus.RUNNING,
                'threadSpecName': 'approval',
                'startTime': '2023-10-30T19:40:51.944Z',
                'childThreadIds': [],
                'parentThreadId': 0,
                'haltReasons': [],
                'currentNodePosition': 2,
                'handledFailedChildren': [],
                'type': ThreadType.CHILD
            }
        ]

        const waitForThreadsRun: NodeRun = {
            id: {
                wfRunId: {
                    id: 'asdf'
                },
                threadRunNumber: 0,
                position: 3
            },
            'status': LHStatus.RUNNING,
            'arrivalTime': '2023-10-30T19:40:52.286Z',
            wfSpecId: {
                name: 'A_WFSPEC',
                majorVersion: 0,
                revision: 0
            },
            'threadSpecName': 'entrypoint',
            'nodeName': '3-threads-WAIT_FOR_THREADS',
            'failures': [],
            'waitThreads': {
                'threads': [
                    {
                        'threadStatus': LHStatus.RUNNING,
                        'threadRunNumber': 1,
                        'waitingStatus': WaitForThreadsRun_WaitingThreadStatus.THREAD_IN_PROGRESS
                    },
                    {
                        'threadStatus': LHStatus.RUNNING,
                        'threadRunNumber': 2,
                        'waitingStatus': WaitForThreadsRun_WaitingThreadStatus.THREAD_IN_PROGRESS
                    }
                ]
            },
            'failureHandlerIds': []
        }

        const associatedThreadRuns: ThreadRun[] = ThreadRunsHandler.getThreadRunsAssociatedWithNodeRun(waitForThreadsRun, threadRuns)

        expect(associatedThreadRuns).toEqual([ threadRuns[1], threadRuns[2] ])
    })

    it('if thread runs were not found an empty result should be provided', () => {
        const threadRuns: ThreadRun[] = [
            {
                wfSpecId: {
                    name: 'A_WFSPEC',
                    majorVersion: 0,
                    revision: 0
                },
                'number': 0,
                'status': LHStatus.RUNNING,
                'threadSpecName': 'entrypoint',
                'startTime': '2023-10-30T19:40:51.912Z',
                'childThreadIds': [
                    1,
                    2
                ],
                'haltReasons': [],
                'currentNodePosition': 3,
                'handledFailedChildren': [],
                'type': ThreadType.ENTRYPOINT
            }
        ]

        const waitForThreadsRun: NodeRun = {
            id: {
                wfRunId: {
                    id: 'asdf'
                },
                threadRunNumber: 0,
                position: 3
            },
            'status': LHStatus.RUNNING,
            'arrivalTime': '2023-10-30T19:40:52.286Z',
            wfSpecId: {
                name: 'A_WFSPEC',
                majorVersion: 0,
                revision: 0
            },
            'threadSpecName': 'entrypoint',
            'nodeName': '3-threads-WAIT_FOR_THREADS',
            'failures': [],
            'waitThreads': {
                'threads': [
                    {
                        'threadStatus': LHStatus.RUNNING,
                        'threadRunNumber': 1,
                        'waitingStatus': WaitForThreadsRun_WaitingThreadStatus.THREAD_IN_PROGRESS
                    }
                ]
            },
            'failureHandlerIds': []
        }

        const associatedThreadRuns: ThreadRun[] = ThreadRunsHandler.getThreadRunsAssociatedWithNodeRun(waitForThreadsRun, threadRuns)

        expect(associatedThreadRuns).not.toContain(undefined)
    })

    it('when the node run does not have wait for threads should return an empty result', () => {
        const threadRuns: ThreadRun[] = [
            {
                wfSpecId: {
                    name: 'A_WFSPEC',
                    majorVersion: 0,
                    revision: 0
                },
                'number': 0,
                'status': LHStatus.RUNNING,
                'threadSpecName': 'entrypoint',
                'startTime': '2023-10-30T19:40:51.912Z',
                'childThreadIds': [
                    1,
                    2
                ],
                'haltReasons': [],
                'currentNodePosition': 3,
                'handledFailedChildren': [],
                'type': ThreadType.ENTRYPOINT
            }
        ]

        const waitForThreadsRun: NodeRun = {
            id: {
                wfRunId: {
                    id: 'asdf'
                },
                threadRunNumber: 0,
                position: 3
            },
            'status': LHStatus.RUNNING,
            'arrivalTime': '2023-10-30T19:40:52.286Z',
            wfSpecId: {
                name: 'A_WFSPEC',
                majorVersion: 0,
                revision: 0
            },
            'threadSpecName': 'entrypoint',
            'nodeName': '3-threads-WAIT_FOR_THREADS',
            'failures': [],
            'failureHandlerIds': []
        }

        const associatedThreadRuns: ThreadRun[] = ThreadRunsHandler.getThreadRunsAssociatedWithNodeRun(waitForThreadsRun, threadRuns)

        expect(associatedThreadRuns).toEqual([])
    })

    it('should return thread run info containing number and thread spec name', () => {
        const threadRunNumber = Math.floor(Math.random() * 10)
        const threadSpecName = faker.animal.type()

        expect(ThreadRunsHandler.buildThreadRunInfo(threadRunNumber, threadSpecName)).toEqual(`{"number": ${threadRunNumber}, "threadSpecName": "${threadSpecName}"}`)
    })
})
