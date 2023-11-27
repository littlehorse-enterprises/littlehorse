import type { ElkLabel, ElkNode } from 'elkjs'
import ElkNodeMapper from './ElkNodeMapper'

describe('elk Node Mapper from LHNode', () => {
    it.each([
        [ '0-entrypoint-ENTRYPOINT', {
            id: '0-entrypoint-ENTRYPOINT',
            labels: [ '0-entrypoint-ENTRYPOINT' ] as ElkLabel[],
            x: 0,
            y: 0,
            width: 141,
            height: 50
        } ],
        [ '0-entrypoint-TASK', {
            id: '0-entrypoint-TASK',
            labels: [ '0-entrypoint-TASK' ] as ElkLabel[],
            x: 0,
            y: 0,
            width: 287,
            height: 50
        } ],
        [ '0-a-name-EXIT', {
            id: '0-a-name-EXIT',
            labels: [ '0-a-name-EXIT' ] as ElkLabel[],
            x: 0,
            y: 0,
            width: 96,
            height: 50
        } ],
        [ '0-a-name-NOP', {
            id: '0-a-name-NOP',
            labels: [ '0-a-name-NOP' ] as ElkLabel[],
            x: 0,
            y: 0,
            width: 237,
            height: 50
        } ],
        [ '0-a-name-START_THREAD', {
            id: '0-a-name-START_THREAD',
            labels: [ '0-a-name-START_THREAD' ] as ElkLabel[],
            x: 0,
            y: 0,
            width: 181,
            height: 50
        } ],
        [ '0-a-name-WAIT_FOR_THREADS', {
            id: '0-a-name-WAIT_FOR_THREADS',
            labels: [ '0-a-name-WAIT_FOR_THREADS' ] as ElkLabel[],
            x: 0,
            y: 0,
            width: 121,
            height: 50
        } ],
        [ '0-a-name-EXTERNAL_EVENT', {
            id: '0-a-name-EXTERNAL_EVENT',
            labels: [ '0-a-name-EXTERNAL_EVENT' ] as ElkLabel[],
            x: 0,
            y: 0,
            width: 237,
            height: 50
        } ],
        [ '0-a-name-SLEEP', {
            id: '0-a-name-SLEEP',
            labels: [ '0-a-name-SLEEP' ] as ElkLabel[],
            x: 0,
            y: 0,
            width: 237,
            height: 50
        } ],
        [ '0-a-name-START_MULTIPLE_THREADS', {
            id: '0-a-name-START_MULTIPLE_THREADS',
            labels: [ '0-a-name-START_MULTIPLE_THREADS' ] as ElkLabel[],
            x: 0,
            y: 0,
            width: 237,
            height: 50
        } ],
        [ '0-a-name-USER_TASK', {
            id: '0-a-name-USER_TASK',
            labels: [ '0-a-name-USER_TASK' ] as ElkLabel[],
            x: 0,
            y: 0,
            width: 287,
            height: 50
        } ],
        [ 'an-invalid-task-type-INVALID', {
            id: 'an-invalid-task-type-INVALID',
            labels: [ 'an-invalid-task-type-INVALID' ] as ElkLabel[],
            x: 0,
            y: 0,
            width: 300,
            height: 50
        } ],
    ]) ('maps %s elkNodeId as reactFlowNodeType', (lhNodeName: string, expectedNodeType: ElkNode | undefined) => {

        expect( ElkNodeMapper.fromLHNode(lhNodeName)).toEqual(expectedNodeType)
    })
})