import NodeTypeMapper from './NodeTypeMapper'

describe('node Type Mapper from ELK to React Flow', () => {
    it.each([
        [ '0-entrypoint-ENTRYPOINT', 'entrypointNodeType' ],
        [ '0-entrypoint-TASK', 'taskNodeType' ],
        [ '0-a-name-EXIT', 'exitNodeType' ],
        [ '0-a-name-NOP', 'nopNodeType' ],
        [ '0-a-name-START_THREAD', 'spawnThreadNodeType' ],
        [ '0-a-name-WAIT_FOR_THREADS', 'waitForThreadsNodeType' ],
        [ '0-a-name-EXTERNAL_EVENT', 'externalEventNodeType' ],
        [ '0-a-name-SLEEP', 'sleepNodeType' ],
        [ '0-a-name-START_MULTIPLE_THREADS', 'spawnMultipleThreadsNodeType' ],
        [ '0-a-name-USER_TASK', 'userTaskNodeType' ],
        [ 'an-invalid-task-type-INVALID', undefined ],
    ]) ('maps %s elkNodeId as %s reactFlowNodeType', (elkNodeId: string, expectedNodeType: string) => {
        expect(NodeTypeMapper.map(elkNodeId)).toEqual(expectedNodeType)
    })
})
