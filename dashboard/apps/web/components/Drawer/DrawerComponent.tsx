import React, { useEffect, useState } from 'react'
import type { Node } from 'reactflow'
import type { ReactFlowNodeWithLHInfo } from '../../app/wfspec/[id]/[version]/components/visualizer/mappers/GraphLayouter'
import { NOPInformation } from './internals/NOPInformation'
import { ExternalEventInformation } from './internals/ExternalEventInformation'
import { SpawnChildInformation } from './internals/SpawnChildInformation'
import { parseKey } from './internals/drawerInternals'
import WfVariable from './WfVariable'
import { SleepNodeInformation } from './internals/SleepNodeInformation'
import { UserTaskNodeInformation } from './internals/UserTaskNodeInformation'
import { WaitForThreadsInformation } from './internals/WaitForThreadsInformation'
import { TaskInformation } from './internals/TaskInformation'
import ThreadRunsHandler from './internals/ThreadRunsHandler'
import type { ThreadVarDef } from '../../littlehorse-public-api/wf_spec'

export interface ThreadRunNameWithRunNumber {
    threadSpecName: string,
    threadRunNumber: number
}
interface DrawerComponentProps {
    isWFRun: boolean
    internalComponent?: string | undefined;
    data?: any;
    graphLayout: any;
    nodeName: string;
    wfRunId?: string;
    setToggleSideBar: (value: boolean) => void;
    setCode: (code: string) => void;
    setLanguage: (language: string) => void;
    setError: (value: boolean) => void;
    setThread: (threadRunNameWithRunNumber: ThreadRunNameWithRunNumber) => void;
    run?: any;
    runs?: any[];
    selectedNode?: ReactFlowNodeWithLHInfo
}

const DEFAULT_THREAD_SPEC_NAME = 'entrypoint'

const DEFAULT_THREAD_RUN_NUMBER = 0

export function DrawerComponent(props: DrawerComponentProps) {
    const [ currentRun, setCurrentRun ] = useState<any>(props.run)
    const [ taskType, setTaskType ] = useState('')
    const [ rawData, setRawData ] = useState<any>()
    const [ wfRunRawData, setWfRunRawData ] = useState<any>()
    const [ selectedNodeData, setSelectedNodeData ] = useState<any>()
    const [ threadSpecVariableDefs, setThreadSpecVariableDefs ] =
        useState<ThreadVarDef[]>()
    const [ selectedNode, setSelectedNode ] = useState<any>()
    const [ wfRunData, setWfRunData ] = useState<any>()
    const [ errorData, setErrorData ] = useState<any>([])
    const [ lastSelectedNode, setLastSelectedNode ] = useState<any>()
    const [ threadName, setThreadName ] = useState<string>()
    const [ threadRunInfoValue, setThreadRunInfoValue ] = useState<string>()

    const setThreadHandler = (thread: string) => {
        const threadRunInfo = JSON.parse(thread)
        if (currentRun !== undefined) {
            setThreadName(threadRunInfo.threadSpecName)
        }
        props.setThread({ threadSpecName: threadRunInfo.threadSpecName, threadRunNumber: threadRunInfo.number })
        setCurrentRun(props.runs?.find(r => r.threadSpecName === threadRunInfo.threadSpecName && r.number === threadRunInfo.number))
        setTaskType('')
    }

    useEffect(() => {
        setThreadHandler(threadRunInfoValue ? threadRunInfoValue : ThreadRunsHandler.buildThreadRunInfo(DEFAULT_THREAD_RUN_NUMBER, DEFAULT_THREAD_SPEC_NAME))
    }, [ threadRunInfoValue ])

    const getErrorData: any = (node: any, key: string) => {
        if (node) {
            const logs = 'task' in node ? node.task.logOutput : null
            const data = node[key].map((element: any) => {
                return {
                    ...element,
                    log: logs,
                }
            })

            setErrorData(data)
        } else {
            setErrorData([])
        }
    }

    useEffect(() => {
        if (props.wfRunId && wfRunRawData === undefined) {
            const fetchWfRun = async () => {
                const response = await fetch(`../../api/drawer/wfRun/${props.wfRunId}`)

                if (response.ok) {
                    response.json().then(result => {
                        setWfRunRawData(result.results)
                    })

                } else {
                    console.error('Error on API while getting wfRun')
                }
            }

            fetchWfRun()
        }

        if (props.data) {
            setThreadSpecVariableDefs(
                (threadName && props.data?.threadSpecs) ? props.data?.threadSpecs[threadName].variableDefs : []
            )

            if (props.nodeName !== lastSelectedNode) {
                setLastSelectedNode(props.nodeName)
                setSelectedNodeData(undefined)
                setWfRunData(undefined)
                setSelectedNode(
                    props.data.threadSpecs[threadName || DEFAULT_THREAD_SPEC_NAME].nodes[
                        props.nodeName
                    ]
                )
                if (props.wfRunId) {
                    const wfRunNode = wfRunRawData.find(
                        (element: any) => element.nodeName === props.nodeName
                    )

                    if (wfRunNode) {
                        getErrorData(wfRunNode, 'failures')
                    } else {
                        setErrorData([])
                    }
                } else {
                    getErrorData(
                        props.data.threadSpecs[threadName || DEFAULT_THREAD_SPEC_NAME].nodes[
                            props.nodeName
                        ],
                        'failureHandlers'
                    )
                }
            }

            const processComplexData = {
                task: () => {
                    if (rawData === undefined) {
                        (async (
                            url: string,
                            name: string,
                            handler: (data: any) => void
                        ) => {
                            const response = await fetch(url + name)

                            if (response.ok) {
                                const content = await response.json()
                                handler(content.data)
                            } else {
                                console.error('INVALID RESPONSE FROM API')
                            }
                        })(
                            '../../api/drawer/taskDef/',
                            selectedNode?.task.taskDefId.name,
                            setRawData
                        )
                    } else {
                        const processedData = rawData.inputVars.map(
                            (
                                element: { name: string; type: string; _: any },
                                index: number
                            ) => {
                                const currentVariable = selectedNode?.task.variables[index]

                                const variableName = currentVariable?.variableName

                                const jsonPath = currentVariable?.jsonPath?.replace('$', '')

                                if (jsonPath) {variableName?.concat(jsonPath)}

                                return {
                                    name: element.name,
                                    type: element.type,
                                    variableName,
                                }
                            }
                        )

                        if (props.wfRunId && wfRunRawData) {
                            const wfRunNode = wfRunRawData.find(
                                (element: any) => element.nodeName === props.nodeName
                            )

                            if (wfRunNode && props.internalComponent) {
                                const data =
                                    wfRunNode[props.internalComponent as keyof typeof wfRunNode]
                                if (data) {
                                    const inputs = (data)?.inputVariables?.map(
                                        (element: any, index: number) => {
                                            const variableType: string = element.value.type
                                            const correctKey = parseKey(variableType) || ''
                                            const value = element.value[correctKey] || ''

                                            processedData[index].value = value

                                            return {
                                                name: element.varName,
                                                type: element.value.type,
                                                value,
                                            }
                                        }
                                    )

                                    const outputs = (data)?.inputVariables?.map(
                                        (element: any) => {
                                            const variableType: string = element.value.type
                                            const correctKey = parseKey(variableType) || ''
                                            const value = element.value[correctKey] || ''

                                            return {
                                                type: element.value.type,
                                                value,
                                            }
                                        }
                                    )

                                    const wfRunComplexData = {
                                        nodeData: {
                                            reachTime: wfRunNode.arrivalTime || '',
                                            completionTime: wfRunNode.endTime || '',
                                            status: wfRunNode.status,
                                        },
                                        inputs,
                                        outputs,
                                    }

                                    setWfRunData(wfRunComplexData)
                                }
                            }
                        }

                        setSelectedNodeData(processedData)
                    }
                },
                externalEvent: () => {
                    if (props.wfRunId && wfRunRawData) {
                        const wfRunNode = wfRunRawData.find(
                            (element: any) => element.nodeName === props.nodeName
                        )

                        if (wfRunNode && props.internalComponent) {
                            const data = wfRunNode[
                                props.internalComponent as keyof typeof wfRunNode
                            ] as {
                                externalEventDefName: string;
                                eventTime: string;
                                externalEventId: {
                                    wfRunId: string;
                                    externalEventDefName: string;
                                    guid: string;
                                };
                            }

                            if (data) {
                                const wfRunComplexData = {
                                    nodeData: {
                                        reachTime: wfRunNode.arrivalTime || '',
                                        completionTime: wfRunNode.endTime || '',
                                        status: wfRunNode.status,
                                    },
                                    guid: data.externalEventId?.guid || '',
                                    arrivedTime: data?.eventTime || '',
                                    arrived: data.eventTime ? 'YES' : 'NO',
                                }

                                setWfRunData(wfRunComplexData)
                            }
                        }
                    }
                },
                nop_def: () => {
                    setSelectedNodeData(selectedNode?.outgoingEdges)
                },
                startThread: () => {
                    if (props.wfRunId && wfRunRawData) {
                        const wfRunNode = wfRunRawData.find(
                            (element: any) => element.nodeName === props.nodeName
                        )

                        if (wfRunNode && props.internalComponent) {
                            const data = wfRunNode[
                                props.internalComponent as keyof typeof wfRunNode
                            ] as {
                                externalEventDefName: string;
                                eventTime: string;
                            }

                            if (data) {
                                const wfRunComplexData = {
                                    nodeData: {
                                        reachTime: wfRunNode.arrivalTime || '',
                                        completionTime: wfRunNode.endTime || '',
                                        status: wfRunNode.status,
                                    },
                                }

                                setWfRunData(wfRunComplexData)
                            }
                        }
                    }
                },
            }

            if (props.internalComponent) {
                const complexData =
                    processComplexData[
                        props.internalComponent as keyof typeof processComplexData
                    ]

                if (complexData instanceof Function) {complexData()}
            }
        }
    }, [
        props.data,
        rawData,
        wfRunRawData,
        selectedNode,
        props.nodeName,
        props.internalComponent,
        threadName
    ])

    useEffect(() => {
        if (!props?.nodeName) {
            setTaskType('')
        } else {
            setTaskType(props.nodeName.split('-').reverse()[0])
        }
    }, [ props.nodeName ])

    useEffect(() => {
        setCurrentRun(props.run)
        setThreadName(props.run?.threadSpecName)
        setThreadSpecVariableDefs(props.run?.variableDefs)
    }, [ props.run ])

    const setToggleSideBar = (
        value: boolean,
        isError: boolean,
        code: string,
        language?: string
    ) => {
        props.setToggleSideBar(value)
        if (
            language === undefined ||
            language === 'jsonObj' ||
            language === 'jsonArr'
        ) {
            props.setCode(JSON.parse(code))
            return
        }
        props.setCode(code)
        props.setLanguage(language)
        props.setError(isError)
    }

    const findNodeByProvidedNodeName = () => {
        return props.graphLayout.nodes.find((node: Node) => node.id === props.nodeName)
    }
    const getThreadRunName = (number: number, threadSpecName: string) => {
        return `${number}-${threadSpecName}`
    }

    return (
        <div className="drawer-component">
            {/* <pre>{threadName &&  props.data.threadSpecs && JSON.stringify(props.data.threadSpecs[threadName].variableDefs, null,2)}</pre> */}
            {/* <pre>{JSON.stringify(props.data, null,2)}</pre> */}
            <div className="drawer__threadSelector">
                <p className="drawer__threadSelector__header">THREADRUN</p>
                <div className="drawer__threadSelector__container">
                    <select
                        className="drawer__threadSelector__container__select"
                        onChange={(event) => {
                            setThreadRunInfoValue(event.target.value)
                        }}
                        value={threadRunInfoValue}
                    >
                        {props.runs?.map(({ threadSpecName, number }) => {
                            return (
                                <option key={number} value={ThreadRunsHandler.buildThreadRunInfo(number, threadSpecName)}>
                                    {getThreadRunName(number, threadSpecName)}
                                </option>
                            )
                        })}
                    </select>
                </div>
                <p className="drawer__threadSelector__header">TYPE</p>
                <div className="drawer__threadSelector__container">
                    <div className="drawer__threadData">{currentRun?.type}</div>
                </div>
            </div>

            <div className="drawer__mainTable">
                <div className="drawer__mainTable__header">ThreadRun Variables</div>
                <div
                    className={`drawer__mainTable__header__subheaders ${
                        props.run
                            ? 'drawer__mainTable__header__subheaders-three-columns'
                            : ''
                    }`}
                >
                    <p className="center ">NAME</p>
                    <p className="center">TYPE</p>
                    {currentRun ? <p className="center">VALUE</p> : null}
                </div>
                {(threadSpecVariableDefs !== undefined && threadSpecVariableDefs.length > 0) ?
                    threadSpecVariableDefs.map((threadSpecVarDef, index) => {
                        return (
                            threadSpecVarDef.varDef &&
                            // eslint-disable-next-line react/no-array-index-key -- we are using the name + index
                            <div key={threadSpecVarDef.varDef.name + index}>
                                <WfVariable
                                    errorData={errorData}
                                    index={index}
                                    name={threadSpecVarDef.varDef.name}
                                    run={props.run}
                                    setToggleSideBar={setToggleSideBar}
                                    threadRunNumber={currentRun.number}
                                    type={threadSpecVarDef.varDef.type}
                                    wfRunId={props.wfRunId}
                                />
                            </div>
                        )
                    }) :
                    (
                        <div className="grid-3 center">
                            No Variables were found
                        </div>
                    )
                }
            </div>


            {/* {props.internalComponent === "task" && (
          <TaskDefInformation
            {...{
              linkedThread: setThreadHandler,
              data: selectedNodeData,
              nodeName: props.nodeName,
              errorData: errorData,
              wfRunData: wfRunData,
              setToggleSideBar: setToggleSideBar,
            }}
          />
        )} */}
            {props.internalComponent === 'nop_def' && (
                <NOPInformation
                    {...{
                        data: selectedNodeData,
                        nodeName: props.nodeName,
                        errorData,
                        setToggleSideBar,
                    }}
                />
            )}

            {props.internalComponent === 'startThread' && (
                <SpawnChildInformation
                    {...{
                        linkedThread: setThreadHandler,
                        nodeName: props.nodeName,
                        errorData,
                        wfRunData,
                        setToggleSideBar,
                    }}
                />
            )}

            {taskType === 'TASK' ?
                <TaskInformation data={findNodeByProvidedNodeName()} isWFRun run={currentRun}
                    setCode={props.setCode}
                    setToggleSideBar={props.setToggleSideBar}
                    wfRunId={props.wfRunId}
                /> : ''}

            {taskType === 'WAIT_FOR_THREADS' ?
                <WaitForThreadsInformation
                    {...{
                        isWFRun: true,
                        run: currentRun,
                        wfRunId: props.wfRunId,
                        linkedThread: setThreadHandler,
                        setThreadRunInfoValue,
                        data: findNodeByProvidedNodeName(),
                        errorData,
                        setToggleSideBar,
                        threadRuns: props.runs,
                    }}
                /> : ''}
            {taskType === 'EXTERNAL_EVENT' ?
                <ExternalEventInformation
                    {...{
                        isWFRun: true,
                        run: currentRun,
                        data: findNodeByProvidedNodeName(),
                        wfRunId: props.wfRunId,
                        nodeName: props.nodeName,
                        errorData,
                        setToggleSideBar,
                    }}
                /> : ''}
            {taskType === 'SLEEP' ?
                <SleepNodeInformation data={findNodeByProvidedNodeName()} isWFRun={props.isWFRun} run={currentRun}
                    wfRunId={props.wfRunId}/> : ''}
            {taskType === 'USER_TASK' ?
                <UserTaskNodeInformation data={findNodeByProvidedNodeName()} isWFRun={props.isWFRun} run={currentRun}
                    setCode={props.setCode}
                    setToggleSideBar={props.setToggleSideBar}
                    wfRunId={props.wfRunId}
                /> : ''}
        </div>
    )
}
