import type { ElkLabel, ElkNode } from 'elkjs'


const fromLHNode = (lhNodeName: string): ElkNode => {
    const INITIAL_X_POSITION = 0
    const INITIAL_Y_POSITION = 0
    const DEFAULT_WIDTH = 300
    const DEFAULT_HEIGHT = 50

    /*
        These values have been stablished by seeing what was the rendered width on the screen for each node type
        the width for each one is defined in the _visualizer.scss file, then the parent elements adds extra width to it, 
        you should pick the viznode-canvas computed width to be here.
        That will help ELK algorithm to determine the correct position of the nodes on the screen because that depends
        on the node width.
        If you change the values here, the values on _visualizer.scss should be adjusted as well.
    */
    const ENTRYPOINT_NODE_WIDTH_WHEN_DRAWN = 141
    const TASK_NODE_WIDTH_WHEN_DRAWN = 287
    const EXIT_NODE_WIDTH_WHEN_DRAWN = 96
    const NOP_NODE_WIDTH_WHEN_DRAWN = 237
    const START_THREAD_NODE_WIDTH_WHEN_DRAWN = 181
    const WAIT_FOR_THREADS_NODE_WIDTH_WHEN_DRAWN = 121
    const EXTERNAL_EVENT_NODE_WIDTH_WHEN_DRAWN = 237
    const SLEEP_WIDTH_WHEN_DRAWN = 237
    const START_MULTIPLE_THREADS_NODE_WIDTH_WHEN_DRAWN = 237
    const USER_TASK_NODE_WIDTH_WHEN_DRAWN = 287

    const defaultElkNode = {
        id: lhNodeName,
        labels: [ lhNodeName ] as ElkLabel[],
        x: INITIAL_X_POSITION,
        y: INITIAL_Y_POSITION,
        width: DEFAULT_WIDTH,
        height: DEFAULT_HEIGHT
    }

    if (lhNodeName.includes('ENTRYPOINT')) {
        return { ...defaultElkNode, width: ENTRYPOINT_NODE_WIDTH_WHEN_DRAWN }
    }

    if (lhNodeName.includes('-TASK')) {
        return { ...defaultElkNode, width: TASK_NODE_WIDTH_WHEN_DRAWN }
    }

    if (lhNodeName.includes('EXIT')) {
        return { ...defaultElkNode, width: EXIT_NODE_WIDTH_WHEN_DRAWN }
    }


    if (lhNodeName.includes('NOP')) {
        return { ...defaultElkNode, width: NOP_NODE_WIDTH_WHEN_DRAWN }
    }

    if (lhNodeName.includes('START_THREAD')) {
        return { ...defaultElkNode, width: START_THREAD_NODE_WIDTH_WHEN_DRAWN }
    }

    if (lhNodeName.includes('WAIT_FOR_THREADS')) {
        return { ...defaultElkNode, width: WAIT_FOR_THREADS_NODE_WIDTH_WHEN_DRAWN }
    }

    if (lhNodeName.includes('EXTERNAL_EVENT')) {
        return { ...defaultElkNode, width: EXTERNAL_EVENT_NODE_WIDTH_WHEN_DRAWN }
    }

    if (lhNodeName.includes('SLEEP')) {
        return { ...defaultElkNode, width: SLEEP_WIDTH_WHEN_DRAWN }
    }

    if (lhNodeName.includes('START_MULTIPLE_THREADS')) {
        return { ...defaultElkNode, width: START_MULTIPLE_THREADS_NODE_WIDTH_WHEN_DRAWN }
    }

    if (lhNodeName.includes('USER_TASK')) {
        return { ...defaultElkNode, width: USER_TASK_NODE_WIDTH_WHEN_DRAWN }
    }

    return {
        id: lhNodeName,
        labels: [ lhNodeName ] as ElkLabel[],
        x: INITIAL_X_POSITION,
        y: INITIAL_Y_POSITION,
        width: DEFAULT_WIDTH,
        height: DEFAULT_HEIGHT
    }
}

const ELKNodeMapper = {
    fromLHNode
}

export default ELKNodeMapper