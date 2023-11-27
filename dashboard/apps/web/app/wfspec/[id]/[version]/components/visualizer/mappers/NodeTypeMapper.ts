const fromNodeKeyName = (keyName: string) => {
    if (keyName.includes('ENTRYPOINT')) {
        return 'entrypointNodeType'
    }

    if (keyName.includes('-TASK')) {
        return 'taskNodeType'
    }

    if (keyName.includes('EXIT')) {
        return 'exitNodeType'
    }

    if (keyName.includes('NOP')) {
        return 'nopNodeType'
    }

    if (keyName.includes('START_THREAD')) {
        return 'spawnThreadNodeType'
    }

    if (keyName.includes('WAIT_FOR_THREADS')) {
        return 'waitForThreadsNodeType'
    }

    if (keyName.includes('EXTERNAL_EVENT')) {
        return 'externalEventNodeType'
    }

    if (keyName.includes('SLEEP')) {
        return 'sleepNodeType'
    }

    if (keyName.includes('START_MULTIPLE_THREADS')) {
        return 'spawnMultipleThreadsNodeType'
    }

    if (keyName.includes('-USER_TASK')) {
        return 'userTaskNodeType'
    }

    return undefined
}

const NodeTypeMapper = {
    map: fromNodeKeyName
}

export default NodeTypeMapper
