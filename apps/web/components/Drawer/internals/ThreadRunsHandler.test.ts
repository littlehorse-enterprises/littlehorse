import { faker } from '@faker-js/faker'
import type { NodeRun } from '../../../littlehorse-public-api/node_run'
import type { ThreadRun } from '../../../littlehorse-public-api/wf_run'
import { ThreadType } from '../../../littlehorse-public-api/wf_run'
import { LHStatus, WaitForThreadsPolicy } from '../../../littlehorse-public-api/common_enums'
import ThreadRunsHandler from './ThreadRunsHandler'
import threadRunsHandler from './ThreadRunsHandler'

describe('handle Thread Runs related logic', () => {
  it('should return all the thread runs that the node run is waiting for', () => {
    const threadRuns: ThreadRun[] = [
      {
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
      'wfRunId': 'asdf',
      'threadRunNumber': 0,
      'position': 3,
      'status': LHStatus.RUNNING,
      'arrivalTime': '2023-10-30T19:40:52.286Z',
      'wfSpecId': {
        'name': 'alti-br',
        'version': 0
      },
      'threadSpecName': 'entrypoint',
      'nodeName': '3-threads-WAIT_FOR_THREADS',
      'failures': [],
      'waitThreads': {
        'threads': [
          {
            'threadStatus': LHStatus.RUNNING,
            'threadRunNumber': 1,
            'alreadyHandled': false
          },
          {
            'threadStatus': LHStatus.RUNNING,
            'threadRunNumber': 2,
            'alreadyHandled': false
          }
        ],
        'policy': WaitForThreadsPolicy.STOP_ON_FAILURE
      },
      'failureHandlerIds': []
    }

    const associatedThreadRuns: ThreadRun[] = ThreadRunsHandler.getThreadRunsAssociatedWithNodeRun(waitForThreadsRun, threadRuns)

    expect(associatedThreadRuns).toEqual([ threadRuns[1], threadRuns[2] ])
  })

  it('if thread runs were not found an empty result should be provided', () => {
    const threadRuns: ThreadRun[] = [
      {
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
      'wfRunId': 'asdf',
      'threadRunNumber': 0,
      'position': 3,
      'status': LHStatus.RUNNING,
      'arrivalTime': '2023-10-30T19:40:52.286Z',
      'wfSpecId': {
        'name': 'alti-br',
        'version': 0
      },
      'threadSpecName': 'entrypoint',
      'nodeName': '3-threads-WAIT_FOR_THREADS',
      'failures': [],
      'waitThreads': {
        'threads': [
          {
            'threadStatus': LHStatus.RUNNING,
            'threadRunNumber': 1,
            'alreadyHandled': false
          }
        ],
        'policy': WaitForThreadsPolicy.STOP_ON_FAILURE
      },
      'failureHandlerIds': []
    }

    const associatedThreadRuns: ThreadRun[] = ThreadRunsHandler.getThreadRunsAssociatedWithNodeRun(waitForThreadsRun, threadRuns)

    expect(associatedThreadRuns).not.toContain(undefined)
  })

  it('when the node run does not have wait for threads should return an empty result', () => {
    const threadRuns: ThreadRun[] = [
      {
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
      'wfRunId': 'asdf',
      'threadRunNumber': 0,
      'position': 3,
      'status': LHStatus.RUNNING,
      'arrivalTime': '2023-10-30T19:40:52.286Z',
      'wfSpecId': {
        'name': 'alti-br',
        'version': 0
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
