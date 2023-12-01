import type { NodeRun } from '../../../littlehorse-public-api/node_run'
import type { ThreadRun } from '../../../littlehorse-public-api/wf_run'

const getThreadRunsAssociatedWithNodeRun = (nodeRun: NodeRun, threadRuns: ThreadRun[]): ThreadRun[] => {
    const relatedThreadRuns: ThreadRun[] = []

    if (nodeRun.waitThreads === undefined) {
        return []
    }

    nodeRun.waitThreads.threads.forEach(nr => {
        const foundThreadRun = threadRuns.find(tr => {
            return tr.number === nr.threadRunNumber
        })

        if (foundThreadRun !== undefined) {
            relatedThreadRuns.push(foundThreadRun)
        }
    })

    return relatedThreadRuns
}

const buildThreadRunInfo = (threadRunNumber: number, threadSpecName: string) => {
    return `{"number": ${threadRunNumber}, "threadSpecName": "${threadSpecName}"}`
}

const ThreadRunsHandler = {
    getThreadRunsAssociatedWithNodeRun,
    buildThreadRunInfo
}

export default ThreadRunsHandler
