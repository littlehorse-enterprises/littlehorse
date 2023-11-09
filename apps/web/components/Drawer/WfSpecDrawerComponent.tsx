import React, { useEffect, useState } from 'react'
import type { Node } from 'reactflow'
import { NOPInformation } from './internals/NOPInformation'
import { ExternalEventInformation } from './internals/ExternalEventInformation'
import { SpawnChildInformation } from './internals/SpawnChildInformation'
import { SleepNodeInformation } from './internals/SleepNodeInformation'
import { UserTaskNodeInformation } from './internals/UserTaskNodeInformation'
import { WaitForThreadsInformation } from './internals/WaitForThreadsInformation'
import { TaskInformation } from './internals/TaskInformation'

interface DrawerComponentProps {
    internalComponent?: string | undefined;
    data: any;
    graphLayout: any;
    nodeName?: string;
    wfSpecId?: string;
    setToggleSideBar: (value: boolean) => void;
    setCode: (code: string) => void;
    setLanguage: (language: string) => void;
    setError: (value: boolean) => void;
    setThread: (value: string) => void;
}

export function WfSpecDrawerComponent(props: DrawerComponentProps) {

    const [ taskType, setTaskType ] = useState('')
    const [ rawData, setRawData ] = useState<any>()
    const [ selectedNodeData, setSelectedNodeData ] = useState<any>()
    const [ threadSpecVariableDefs, setThreadSpecVariableDefs ] =
        useState<{ name: string; type: string; value?: string }[]>()
    const [ selectedNode, setSelectedNode ] = useState<any>()
    const [ wfRunData, setWfRunData ] = useState<any>()
    // eslint-disable-next-line react/hook-use-state -- seems is not used, analyze it further https://littlehorse.atlassian.net/browse/LH-237
    const [ errorData ] = useState<any>([])
    const [ lastSelectedNode, setLastSelectedNode ] = useState<any>()
    const [ threadName, setThreadName ] = useState<string>()

    const setThreadHandler = (thread: string) => {
        setThreadName(thread)
        props.setThread(thread)
        setTaskType('')
    }

    const getData: any = async (
        url: string,
        name: string,
        handler: (data: any) => void,
        dataPath: string
    ) => {
        const response = await fetch(url + name)

        if (response.ok) {
            const content = await response.json()

            handler(content.data[dataPath])
        } else {console.error('INVALID RESPONSE FROM API')}
    }


    useEffect(() => {


        if (props.data) {
            if (threadSpecVariableDefs === undefined || threadName) {
                let selectedThread

                if (threadName === undefined) {
                    selectedThread = props.data.entrypointThreadName
                    selectedThread
                }

                setThreadSpecVariableDefs(
                    props.data.threadSpecs[selectedThread || threadName].variableDefs
                )
            }

            if (props.nodeName && props.nodeName !== lastSelectedNode) {
                setLastSelectedNode(props.nodeName)
                setSelectedNodeData(undefined)
                setWfRunData(undefined)
                setSelectedNode(
                    props.data.threadSpecs[threadName || 'entrypoint'].nodes[
                        props.nodeName
                    ]
                )

            }

            const processComplexData = {
                task: () => {
                    if (rawData === undefined) {
                        getData(
                            '../../api/drawer/taskDef/',
                            selectedNode?.task.taskDefName,
                            setRawData,
                            'result'
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


                        setSelectedNodeData(processedData)
                    }
                },
                externalEvent: () => {
                    const processedData = selectedNode?.variableMutations.map(
                        (element: {
                            lhsName: string;
                            operation: string;
                            nodeOutput: any;
                        }) => {
                            let literalValue =
                                selectedNode.externalEvent.externalEventDefName

                            if (Object.prototype.hasOwnProperty.call(element.nodeOutput, 'jsonpath')) {console.error('Missing fix of property: jsonpath')}

                            if (Object.prototype.hasOwnProperty.call(element.nodeOutput,'jsonPath')) {
                                console.error(
                                    'Property fixed: jsonPath; NEED TO SUBSTITUTE ON CODE'
                                )
                            }

                            //TODO: verify that the jsonPath property is right spelled
                            const jsonPath = element.nodeOutput?.jsonpath?.replace('$', '')

                            if (jsonPath) {literalValue = literalValue + jsonPath}

                            // FIXME: why is the concat not working?
                            //literalValue.concat(jsonPath)

                            return {
                                mutatedVariable: element.lhsName,
                                mutatedType: element.operation,
                                literalValue,
                            }
                        }
                    )

                    setSelectedNodeData(processedData)
                },
                nop_def: () => {
                    setSelectedNodeData(selectedNode?.outgoingEdges)
                }
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
        selectedNode,
        props.nodeName,
        props.internalComponent,
        threadName,
    ])

    useEffect(() => {
        if (!props?.nodeName) {return}
        setTaskType(props.nodeName.split('-').reverse()[0])
    }, [ props.nodeName ])

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

    return (
        <div className="drawer-component">
            <div className="drawer__threadSelector">
                <p className="drawer__threadSelector__header">THREADSPEC NAME</p>
                <div className="drawer__threadSelector__container">
                    <select
                        className="drawer__threadSelector__container__select"
                        onChange={(event) => { setThreadHandler(event.target.value) }}
                        value={threadName}
                    >
                        {props.data ? Object.keys(props.data.threadSpecs).map((name, index) => {
                            return (
                            // eslint-disable-next-line react/no-array-index-key -- we are using the name + index
                                <option key={name + index} value={name}>
                                    {name}
                                </option>
                            )
                        }) : null}
                    </select>
                </div>
            </div>
            <div className="drawer__mainTable">
                <div className="drawer__mainTable__header">
                    ThreadSpec Variables
                </div>
                <div
                    className={`drawer__mainTable__header__subheaders `}
                >
                    <p className="center ">NAME</p>
                    <p className="center">TYPE</p>
                </div>
                {(threadSpecVariableDefs !== undefined && threadSpecVariableDefs.length > 0) ?
                    threadSpecVariableDefs.map(({ name, type }, index) => {
                        return (
                        // eslint-disable-next-line react/no-array-index-key -- we are using the name + index
                            <div key={name + index}>
                
                                {/* eslint-disable-next-line react/no-array-index-key -- we are using the name + index */}
                                <div className={`grid-2 `} key={name + index}>
                                    <p className="center">{name}</p>
                                    <p className="center">{type}</p>
                                </div>
                            </div>
                        )
                    }) : (
                        <div className="grid-2 center">
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
              nodeName: props.nodeName || '',
              errorData: errorData,
              wfRunData: wfRunData,
              setToggleSideBar: setToggleSideBar,
            }}
          />
        )} */
            }
            {
                props.internalComponent === 'nop_def' && (
                    <NOPInformation
                        {...{
                            data: selectedNodeData,
                            nodeName: props.nodeName || '',
                            errorData,
                            setToggleSideBar,
                        }}
                    />
                )
            }

            {
                props.internalComponent === 'startThread' && (
                    <SpawnChildInformation
                        {...{
                            linkedThread: setThreadHandler,
                            nodeName: props.nodeName,
                            errorData,
                            wfRunData,
                            setToggleSideBar,
                        }}
                    />
                )
            }

            {
                taskType === 'TASK' ?
                    <TaskInformation data={findNodeByProvidedNodeName()}
                        isWFRun={false}
                        setCode={props.setCode}
                        setToggleSideBar={props.setToggleSideBar}
                    /> : ''
            }

            {
                taskType === 'WAIT_FOR_THREADS' ?
                    <WaitForThreadsInformation
                        {...{
                            isWFRun: false,
                            linkedThread: setThreadHandler,
                            data: findNodeByProvidedNodeName(),
                            errorData,
                            setToggleSideBar
                        }}
                    /> : ''
            }
            {
                taskType === 'EXTERNAL_EVENT' ?
                    <ExternalEventInformation
                        {...{
                            isWFRun: false,
                            data: findNodeByProvidedNodeName(),
                            errorData,
                            setToggleSideBar,
                        }}
                    /> : ''
            }
            {
                taskType === 'SLEEP' ? <SleepNodeInformation data={findNodeByProvidedNodeName()} isWFRun={false}/> : ''
            }
            {
                taskType === 'USER_TASK' ? <UserTaskNodeInformation data={findNodeByProvidedNodeName()}
                    isWFRun={false}
                    setCode={props.setCode}
                    setToggleSideBar={props.setToggleSideBar}/> : ''
            }
        </div>
    )
        
}
