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
    version: number
}

interface mapnode {

}

export function WfSpecVisualizer({ id, version }: WfSpecVisualizerProps) {

  const [ rawdata, setRawData ] = useState<any[]>([])
  const [ wfSpec, setWfSpec ] = useState<any[]>([])
  const [ drawerData, setDrawerData ] = useState<any>()
  const [ selectedNodeName, setSelectedNodeName ] = useState<any>()
  const [ nodeType, setNodeType ] = useState<string | undefined>()
  const [ loading, setLoading ] = useState(true)

  const [ language, setLanguage ] = useState<any>()
  const [ showError, setShowError ] = useState(false)
  const [ toggleSideBar, setToggleSideBar ] = useState(false)
  const [ sideBarData, setSideBarData ] = useState('')
  const [ threadSpec, setThreadSpec ] = useState('')
  const [ graphLayout, setGraphLayout ] = useState()

  const setGraphWithNodeRunPosition = (graph) => {
    setGraphLayout({ ...graph })
  }

  const mapData = (data: any, thread?: string) => {
    const threads = Object.keys(data?.threadSpecs)
    const print_thread = thread || threads[0]
    const entries = Object.entries(data?.threadSpecs?.[print_thread]?.nodes)
    const mappedData: any = entries.map((e: mapnode) => ({
      name: e[0],
      type: e[0].split('-').pop(),
      position: e[0].split('-').shift(),
      node: e[1],
      childs: e[1].outgoingEdges.map(e => e.sinkNodeName),
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
      const wfSpec = await res.json()
      setRawData(wfSpec)
      setWfSpec(mapData(wfSpec))
    }
  }
  const setThread = (thread: string) => {
    setSelectedNodeName(undefined)
    setWfSpec(mapData(rawdata, thread))
    setThreadSpec(thread)
  }

  useEffect(() => {
    if (drawerData === undefined) getMainDrawerData(id, setDrawerData)

    if (selectedNodeName) {
      const nodePostFix = selectedNodeName.split('-').reverse()[0]

      setNodeType(nodeTypes[nodePostFix as keyof typeof nodeTypes])
    }
  }, [ drawerData, selectedNodeName ])

  useEffect(() => {
    getWfSpec()
  }, [])

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
            wfSpecName={id}
            wfSpecVersion={version}
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
