"use client";
import { useEffect, useState } from "react";
import WFRunInformationSideBar from "../../../../../../components/WFRunInformationSideBar";
import { DrawerComponent } from "../../../../../../components/Drawer/DrawerComponent";
import { Drawer } from "../../../../../../components/Drawer/Drawer";
import {
  getMainDrawerData,
  nodeTypes,
} from "../../../../../../components/Drawer/internals/drawerInternals";
import { WfRunVisualizerChart } from "./WfRunVisualizerChart";
import { Loader } from "ui";

interface mapnode {}
export const WfRunVisualizer = ({
  id,
  wfspec,
}: {
  id: string;
  wfspec: string;
}) => {
  const [data, setData] = useState<any[]>([]);
  const [drawerData, setDrawerData] = useState<any>();
  const [selectedNodeName, setSelectedNodeName] = useState<any>();
  const [nodeType, setNodeType] = useState<string | undefined>();
  const [loading, setLoading] = useState(true);

  const [language, setLanguage] = useState<any>();
  const [showError, setShowError] = useState(false);
  const [toggleSideBar, setToggleSideBar] = useState(false);
  const [sideBarData, setSideBarData] = useState("");

  const rec = (mappedData, i) => {
    let el = mappedData[i];
    if (!el.childs.length) return mappedData; //if not childs close the REC function
    if (el.type === "WAIT_FOR_THREAD") {
      let wft = el.node.waitForThread.threadRunNumber.variableName;
      let thread = mappedData.find((m) => m.name === wft);
      el.wlevel = thread.level;
    }
    mappedData = mappedData.map((m) => {
      if (el.childs.includes(m.name)) {
        m.level = el.level + 1; // each child heritate parent level + 1
        if (m.type === "NOP") {
          m.px = "center";
        } else {
          m.px = el.px;
        }
        if (el.childs.length > 1) {
          m.level = el.level + 2;
          m.px = m.name === el.childs[0] ? "left" : "right";
        }
        if (m.type === "NOP" && m.childs.length === 1) {
          el.cNOP = m.name;
        }
      }
      return m;
    });
    return rec(mappedData, ++i);
  };

  const [run, setRun] = useState();
  const setThreads = (data: any) => {
    // console.log('threads',data)
    getWfSpec(data.wfSpecName, data.wfSpecVersion);
    setRun(data.threadRuns[0]);
  };

  const mapData = (data: any) => {
    const entries = Object.entries(data?.threadSpecs?.entrypoint?.nodes);
    const mappedData: any = entries.map((e: mapnode) => ({
      name: e[0],
      type: e[0].split("-").pop(),
      node: e[1],
      childs: e[1]["outgoingEdges"].map((e) => e.sinkNodeName),
      level: 0,
      px: "center",
    }));
    setLoading(false);
    return rec(mappedData, 0);
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
      setData(mapData(content.result));
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

  useEffect(() => {
    getData();
  }, []);

  useEffect(() => {
    if (drawerData === undefined) getMainDrawerData(wfspec, setDrawerData);

    if (selectedNodeName) {
      const nodePostFix = selectedNodeName.split("-").reverse()[0];

      setNodeType(nodeTypes[nodePostFix as keyof typeof nodeTypes]);
    }
  }, [drawerData, selectedNodeName]);

  const drawerInternal = (
    <DrawerComponent
      internalComponent={nodeType}
      data={drawerData}
      nodeName={selectedNodeName}
      wfRunId={id}
      setToggleSideBar={setToggleSideBar}
      setCode={setSideBarData}
      setLanguage={setLanguage}
      setError={setShowError}
      run={run}
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
