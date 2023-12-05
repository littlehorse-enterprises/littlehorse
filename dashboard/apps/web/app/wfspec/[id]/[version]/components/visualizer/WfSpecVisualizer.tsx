'use client'
import React, { useEffect, useState } from 'react'
import { Loader } from 'ui'
import { Drawer } from '../../../../../../components/Drawer/Drawer'
import { getMainDrawerData, nodeTypes } from '../../../../../../components/Drawer/internals/drawerInternals'
import WFRunInformationSideBar from '../../../../../../components/WFRunInformationSideBar'
import { WfSpecDrawerComponent } from '../../../../../../components/Drawer/WfSpecDrawerComponent'
import { WfSpecGraph } from './WfSpecGraph'

interface WfSpecVisualizerProps {
    id: string
    version: string
}

export function WfSpecVisualizer({ id, version }: WfSpecVisualizerProps) {

    const [ rawData, setRawData ] = useState<any[]>([])
    const [ wfSpec, setWfSpec ] = useState<any[]>([])
    const [ drawerData, setDrawerData ] = useState<any>()
    const [ selectedNodeName, setSelectedNodeName ] = useState<any>()
    const [ nodeType, setNodeType ] = useState<string | undefined>()
    const [ loading, setLoading ] = useState(true)

    const [ language, setLanguage ] = useState<any>()
    const [ showError, setShowError ] = useState(false)
    const [ toggleSideBar, setToggleSideBar ] = useState(false)
    const [ sideBarData, setSideBarData ] = useState('')
    const [ threadSpec, setThreadSpec ] = useState('entrypoint')
    const [ graphLayout, setGraphLayout ] = useState()

    const setGraphWithNodeRunPosition = (graph) => {
        setGraphLayout({ ...graph })
    }

    const mapData = (data: any, thread?: string) => {
        const threads = Object.keys(data?.threadSpecs)
        const printThread = thread || threads[0]
        const entries = Object.entries(data?.threadSpecs?.[printThread]?.nodes)
        const mappedData: any = entries.map((e: any) => ({
            name: e[0],
            type: e[0].split('-').pop(),
            position: e[0].split('-').shift(),
            node: e[1],
            childs: e[1].outgoingEdges.map(outgoingEdge => outgoingEdge.sinkNodeName),
            level: 0,
            closer: false,
            while: false,
            px: 'center'
        }))

        setLoading(false)

        return mappedData
    }
    const getWfSpec = async () => {
        const res = await fetch('/api/visualization/wfSpec', {
            method: 'POST',
            body: JSON.stringify({
                id,
                version
            })
        })
        if (res.ok) {
            const foundWfSpec = await res.json()
            setRawData(foundWfSpec)
            setWfSpec(mapData(foundWfSpec))
        }
    }
    const setThread = (thread: string) => {
        setSelectedNodeName(undefined)
        setWfSpec(mapData(rawData, thread))
        setThreadSpec(thread)
    }

    useEffect(() => {
        if (drawerData === undefined) {getMainDrawerData(id, setDrawerData)}

        if (selectedNodeName) {
            const nodePostFix = selectedNodeName.split('-').reverse()[0]

            setNodeType(nodeTypes[nodePostFix as keyof typeof nodeTypes])
        }
    }, [ drawerData, selectedNodeName ])

    useEffect(() => {
        getWfSpec()
    }, [])

    const getVersionFromFormattedString = (formattedVersion: string) => {
        const versionValues = formattedVersion.split('.')
        return {
            majorVersion: versionValues[0],
            revision: versionValues[1]
        }
    }

    return (

        <div className='visualizer'>
            <div
                className={`canvas scrollbar ${wfSpec.length === 0 ? 'flex items-center justify-items-center justify-center' : ''}`}>

                {loading ? (
                    <Loader/>
                ) : (

                    <WfSpecGraph isWfSpecVisualization
                        setGraphWithNodeRunPosition={setGraphWithNodeRunPosition}
                        setSelectedNodeName={setSelectedNodeName}
                        threadRunNumber={null}
                        threadSpec={threadSpec}
                        wfRunId={null}
                        wfSpecMajorVersion={Number(getVersionFromFormattedString(version).majorVersion)}
                        wfSpecName={id}
                        wfSpecRevision={Number(getVersionFromFormattedString(version).revision)}
                    />
                )
                }
            </div>

            <Drawer title="WfSpec Properties">
                <WfSpecDrawerComponent
                    data={drawerData}
                    graphLayout={graphLayout}
                    internalComponent={nodeType}
                    nodeName={selectedNodeName}
                    setCode={setSideBarData}
                    setError={setShowError}
                    setLanguage={setLanguage}
                    setThread={setThread}
                    setToggleSideBar={setToggleSideBar}
                    wfSpecId={id}
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
