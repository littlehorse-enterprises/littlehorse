'use client'
import React, { useEffect, useState } from 'react'
import { Loader } from 'ui'
import WFRunInformationSideBar from '../../../../../components/WFRunInformationSideBar'
import type { ThreadRunNameWithRunNumber } from '../../../../../components/Drawer/DrawerComponent'
import { DrawerComponent } from '../../../../../components/Drawer/DrawerComponent'
import { Drawer } from '../../../../../components/Drawer/Drawer'
import { nodeTypes, } from '../../../../../components/Drawer/internals/drawerInternals'
import { WfSpecGraph } from '../../../../wfspec/[id]/[version]/components/visualizer/WfSpecGraph'
import type { ReactFlowGraph } from '../../../../wfspec/[id]/[version]/components/visualizer/mappers/GraphLayouter'

export function WfRunVisualizer({
    wfRunId
}: {
    wfRunId: string;
}) {
    const [ wfSpec, setWfSpec ] = useState<any[]>([])
    const [ selectedNodeName, setSelectedNodeName ] = useState<any>()
    const [ nodeType, setNodeType ] = useState<string | undefined>()
    const [ loading, setLoading ] = useState(true)

    const [ language, setLanguage ] = useState<any>()
    const [ showError, setShowError ] = useState(false)
    const [ toggleSideBar, setToggleSideBar ] = useState(false)
    const [ sideBarData, setSideBarData ] = useState('')
    const [ wfSpecMajorVersion, setWfSpecMajorVersion ] = useState(0)
    const [ wfSpecRevision, setWfSpecRevision ] = useState(0)
    const [ wfSpecName, setWfSpecName ] = useState()
    const [ graphLayout, setGraphLayout ] = useState()
    const [ threadRunSpecWithNumber, setThreadRunSpecWithNumber ]
        = useState<ThreadRunNameWithRunNumber>({ threadSpecName: 'entrypoint', threadRunNumber: 0 })

    const setGraphWithNodeRunPosition = (graph) => {
        setGraphLayout({ ...graph })
    }
    // const getLoops = async (taskDefName, wfRunId) => {
    //   const res = await fetch('/api/loops/taskRun', {
    //     method: 'POST',
    //     body: JSON.stringify({
    //       taskDefName,
    //       wfRunId
    //     }),
    //   })
    //   if (res.ok) {
    //     const results = await res.json()
    //     console.log('RESS', results.length)
    //
    //     return results.length > 1
    //     //  setLoops(results)
    //   }
    // }

    const [ run, setRun ] = useState<any>()
    const [ runs, setRuns ] = useState<any[]>([])
    const setThreads = (data: any) => {
        getWfSpec(data.wfSpecId.name, data.wfSpecId.majorVersion, data.wfSpecId.revision)
        setWfSpecMajorVersion(data.wfSpecId.majorVersion)
        setWfSpecRevision(data.wfSpecId.revision)
        setRun(data.threadRuns[0])
        setRuns(data.threadRuns)
        setWfSpecName(data.wfSpecId.name)
    }

    const getWfSpec = async (wfSpecNameToSearchFor: string, majorVersion: number, revision: number) => {
        const wfSpecResponse = await fetch('/api/visualization/wfSpec', {
            method: 'POST',
            body: JSON.stringify({
                id: wfSpecNameToSearchFor,
                majorVersion,
                revision
            }),
        })

        if (wfSpecResponse.ok) {
            const content = await wfSpecResponse.json()

            if (content) {
                setWfSpec(content)
            }
        }
    }
    const getData = async () => {
        const res = await fetch('/api/visualization/wfRun', {  // TODO; new ALGO
            method: 'POST',
            body: JSON.stringify({
                wfRunId,
            }),
        })
        if (res.ok) {
            const response = await res.json()
            setThreads(response)
        }
    }

    const updateSelectedThreadRun = (threadRunNameWithRunNumber: ThreadRunNameWithRunNumber) => {
        setThreadRunSpecWithNumber({ ...threadRunNameWithRunNumber })
        setSelectedNodeName(undefined)
    }

    useEffect(() => {
        getData()
        setLoading(false)
    }, [])

    useEffect(() => {
        if (selectedNodeName) {
            const nodeTypeFromName = selectedNodeName.split('-').reverse()[0]

            setNodeType(nodeTypes[nodeTypeFromName as keyof typeof nodeTypes])
        }
    }, [ selectedNodeName ])

    return (
        <div className="visualizer">
            <div
                className={`canvas scrollbar ${
                    graphLayout === undefined || (graphLayout as ReactFlowGraph).nodes.length === 0
                        ? 'flex items-center justify-items-center justify-center'
                        : ''
                }`}>
                {loading ? (
                    <Loader/>
                ) : (
                    <WfSpecGraph isWfSpecVisualization={false}
                        setGraphWithNodeRunPosition={setGraphWithNodeRunPosition}
                        setSelectedNodeName={setSelectedNodeName}
                        threadRunNumber={threadRunSpecWithNumber.threadRunNumber}
                        threadSpec={threadRunSpecWithNumber.threadSpecName}
                        wfRunId={wfRunId}
                        wfSpecMajorVersion={wfSpecMajorVersion}
                        wfSpecName={wfSpecName}
                        wfSpecRevision={wfSpecRevision}
                    />
                )}

            </div>

            <Drawer title="WfSpec Properties">
                <DrawerComponent
                    data={wfSpec}
                    graphLayout={graphLayout}
                    internalComponent={nodeType}
                    isWFRun
                    nodeName={selectedNodeName}
                    run={run}
                    runs={runs}
                    setCode={setSideBarData}
                    setError={setShowError}
                    setLanguage={setLanguage}
                    setThread={updateSelectedThreadRun}
                    setToggleSideBar={setToggleSideBar}
                    wfRunId={wfRunId}
                />
            </Drawer>
            <WFRunInformationSideBar
                errorLog={showError}
                language={language}
                output={sideBarData}
                setToggleSideBar={setToggleSideBar}
                toggleSideBar={toggleSideBar}
            />
        </div>
    )
}
