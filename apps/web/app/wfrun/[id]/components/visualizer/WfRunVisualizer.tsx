"use client";
import { useEffect, useState } from "react";
import WFRunInformationSideBar from "../../../../../components/WFRunInformationSideBar";
import { DrawerComponent } from "../../../../../components/Drawer/DrawerComponent";
import { Drawer } from "../../../../../components/Drawer/Drawer";
import {
  getMainDrawerData,
  nodeTypes,
} from "../../../../../components/Drawer/internals/drawerInternals";
import { WfRunVisualizerChart } from "./WfRunVisualizerChart";
import { Loader } from "ui";

interface mapnode {}
export const WfRunVisualizer = ({
  id
}: {
  id: string;
}) => {
  const [rawdata, setRawData] = useState<any[]>([])
  const [data, setData] = useState<any[]>([]);
  const [drawerData, setDrawerData] = useState<any>();
  const [selectedNodeName, setSelectedNodeName] = useState<any>();
  const [nodeType, setNodeType] = useState<string | undefined>();
  const [loading, setLoading] = useState(true);

  const [language, setLanguage] = useState<any>();
  const [showError, setShowError] = useState(false);
  const [toggleSideBar, setToggleSideBar] = useState(false);
  const [sideBarData, setSideBarData] = useState("");

  const rec = (mappedData, i, offset, open=false) => {
    let el = mappedData[i];
    el.level=+el.position+offset
    if (!el.childs.length) return mappedData; //if not childs close the REC function
    if (el.type === "WAIT_FOR_THREAD") {
      let wft = el.node.waitForThread.threadRunNumber.variableName;
      let thread = mappedData.find((m) => m.name === wft);
      el.wlevel = thread.level;
    }

    let addo = 0
		if(el.type === 'NOP' ){
			if(open){
				el.closer = true
				open = false
			}else{
				open = true
				addo = 1
			}
		}

    mappedData = mappedData.map((m) => {
      if (el.childs.includes(m.name)) {
        // m.level = el.level + 1; // each child heritate parent level + 1

        // CHECK IF NOP IS WHILE
				if(el.type === 'NOP' && m.type==='NOP'){
					const econd =  el.node.outgoingEdges.find(e => e.sinkNodeName != m.name)?.condition || {}
					const mcond = m.node.outgoingEdges.find(e => e.sinkNodeName === el.name)?.condition || {}
					if(JSON.stringify(econd) === JSON.stringify(mcond)){
						el.while = true
						m.while = true
					}
				}

        if (m.type === "NOP") {
          m.px = "center";
        } else {
          m.px = el.px;
        }

        if (el.childs.length > 1 && (m.type != 'NOP') ) {
					// m.level = el.level + 2
					m.px = m.name === el.childs[0] ? 'left' : 'right'
				}
				// if(m.type === 'NOP' && m.childs.length === 1){
	
				if(m.type === 'NOP' && open){
					el.cNOP = m.name
				}
				if(!open){
					m.px = 'center'
				}
        // if (el.childs.length > 1) {
        //   m.level = el.level + 2;
        //   m.px = m.name === el.childs[0] ? "left" : "right";
        // }
        // if (m.type === "NOP" && m.childs.length === 1) {
        //   el.cNOP = m.name;
        // }
      }
      return m;
    });
    return rec(mappedData, ++i, offset+addo, open)
  };

  const [run, setRun] = useState();
  const [runs, setRuns] = useState<any[]>([]);
  const setThreads = (data: any) => {
    getWfSpec(data.wfSpecName, data.wfSpecVersion);
    setRun(data.threadRuns[0]);
    setRuns(data.threadRuns);
  };

  const mapData = (data: any, thread?: string) => {
    const threads = Object.keys(data?.threadSpecs)
		const print_thread = thread || threads[0]
    const entries = Object.entries(data?.threadSpecs?.[print_thread]?.nodes);
    const mappedData: any = entries.map((e: mapnode) => ({
      name: e[0],
      type: e[0].split("-").pop(),
      position: e[0].split("-").shift(),
      node: e[1],
      childs: e[1]["outgoingEdges"].map((e) => e.sinkNodeName),
      level: 0,
      closer:false,
      while:false,
      px: "center",
    }));
    setLoading(false);
    return rec(mappedData, 0, 0)
  };

  const getWfSpec = async (id: string, version: number) => {
    const res = await fetch("/api/visualization/wfSpec", {
      method: "POST",
      body: JSON.stringify({
        id,
        version,
      }),
    });
    if (res.ok) {
      const content = await res.json();
      if(content.result){
        setRawData(content.result)
        setData(mapData(content.result))
      }
    }
  };
  const getData = async () => {
    const res = await fetch("/api/visualization/wfRun", {
      method: "POST",
      body: JSON.stringify({
        id,
      }),
    });
    if (res.ok) {
      const { result } = await res.json();
      setThreads(result);
    }
  };

  const setThread = (thread:string) => {
		setData(mapData(rawdata,thread))
	}

  useEffect(() => {
    getData();
  }, []);

  useEffect(() => {
    // if (drawerData === undefined) getMainDrawerData(run?.wfSpecName || '', setDrawerData);
    
    if (selectedNodeName) {
      const nodePostFix = selectedNodeName.split("-").reverse()[0];

      setNodeType(nodeTypes[nodePostFix as keyof typeof nodeTypes]);
    }
  }, [drawerData, selectedNodeName]);

  const drawerInternal = (
    <DrawerComponent
      isWFRun={true}
      internalComponent={nodeType}
      datao={data}
      data={rawdata}
      nodeName={selectedNodeName}
      wfRunId={id}
      setToggleSideBar={setToggleSideBar}
      setCode={setSideBarData}
      setLanguage={setLanguage}
      setError={setShowError}
      setThread={setThread}
      run={run}
      runs={runs}
    />
  );

  return (
    <div className="visualizer">
      <div
        className={`canvas scrollbar ${
          data.length === 0
            ? "flex items-center justify-items-center justify-center"
            : ""
        }`}
      >
        {loading ? (
          <Loader />
        ) : (
          <WfRunVisualizerChart
            data={data}
            onClick={setSelectedNodeName}
            run={run}
          />
        )}
      </div>
      <Drawer title={"WfSpec Properties"}>{drawerInternal}</Drawer>
      <WFRunInformationSideBar
        toggleSideBar={toggleSideBar}
        setToggleSideBar={setToggleSideBar}
        output={sideBarData}
        errorLog={showError}
        language={language}
      />
    </div>
  );
};
