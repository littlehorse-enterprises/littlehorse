import React, { useEffect, useState } from "react";
import { TaskDefInformation } from "./internals/TaskDefInformation";
import { NOPInformation } from "./internals/NOPInformation";
import { ExternalEventInformation } from "./internals/ExternalEventInformation";
import { SpawnChildInformation } from "./internals/SpawnChildInformation";
import { WaitChildInformation } from "./internals/WaitChildInformation";
import { SleepNodeInformation } from "./internals/SleepNodeInformation";

interface DrawerComponentProps {
  internalComponent?: string | undefined;
  data: any;
  datao: any;
  nodeName?: string;
  wfSpecId?: string;
  setToggleSideBar: (value: boolean) => void;
  setCode: (code: string) => void;
  setLanguage: (language: string) => void;
  setError: (value: boolean) => void;
  setThread: (value: string) => void;
}

export const WfSpecDrawerComponent = (props: DrawerComponentProps) => {
  const [type, setType] = useState('');
  const [rawData, setRawData] = useState<any>();
  const [wfRunRawData, setWfRunRawData] = useState<any>();
  const [selectedNodeData, setSelectedNodeData] = useState<any>();
  const [mainData, setMainData] =
    useState<{ name: string; type: string; value?: string }[]>();
  const [selectedNode, setSelectedNode] = useState<any>();
  const [wfRunData, setWfRunData] = useState<any>();
  const [errorData, setErrorData] = useState<any>([]);
  const [lastSelectedNode, setLastSelectedNode] = useState<any>();
  const [threadName, setThreadName] = useState<string>();

  const setThreadHandler = (thread: string) => {
    setThreadName(thread);
    props.setThread(thread);
  };

  const getData: any = async (
    url: string,
    name: string,
    handler: (data: any) => void,
    dataPath: string
  ) => {
    const response = await fetch(url + name);

    if (response.ok) {
      const content = await response.json();

      handler(content.data[dataPath]);
    } else console.warn("INVALID RESPONSE FROM API");
  };


  useEffect(() => {


    if (props.data) {
      if (mainData === undefined || threadName) {
        let selectedThread;

        if (threadName === undefined) {
          selectedThread = props.data.entrypointThreadName;
          selectedThread;
        }

        setMainData(
          props.data.threadSpecs[selectedThread || threadName].variableDefs
        );
      }

      if (props.nodeName && props.nodeName !== lastSelectedNode) {
        setLastSelectedNode(props.nodeName);
        setSelectedNodeData(undefined);
        setWfRunData(undefined);
        setSelectedNode(
          props.data.threadSpecs[threadName || "entrypoint"].nodes[
            props.nodeName
          ]
        );

      }

      const processComplexData = {
        task: () => {
          console.log("selectedNode", selectedNode);
          if (rawData === undefined)
            getData(
              "../../api/drawer/taskDef/",
              selectedNode?.task.taskDefName,
              setRawData,
              "result"
            );
          else {
            const processedData = rawData.inputVars.map(
              (
                element: { name: string; type: string; _: any },
                index: number
              ) => {
                const currentVariable = selectedNode?.task.variables[index];

                let variableName = currentVariable?.variableName;

                const jsonPath = currentVariable?.jsonPath?.replace("$", "");

                if (jsonPath) variableName?.concat(jsonPath);

                return {
                  name: element.name,
                  type: element.type,
                  variableName: variableName,
                };
              }
            );



            setSelectedNodeData(processedData);
          }
        },
        externalEvent: () => {

          console.log(selectedNode?.variableMutations)
          const processedData = selectedNode?.variableMutations.map(
            (element: {
              lhsName: string;
              operation: string;
              nodeOutput: any;
            }) => {
              let literalValue =
                selectedNode.externalEvent.externalEventDefName;

              if (element.nodeOutput?.hasOwnProperty("jsonpath"))
                console.warn("Missing fix of property: jsonpath");

              if (element.nodeOutput?.hasOwnProperty("jsonPath"))
                console.warn(
                  "Property fixed: jsonPath; NEED TO SUBSTITUTE ON CODE"
                );

              //TODO: verify that the jsonPath property is right spelled
              const jsonPath = element.nodeOutput?.jsonpath?.replace("$", "");

              if (jsonPath) literalValue = literalValue + jsonPath;

              // FIXME: why is the concat not working?
              //literalValue.concat(jsonPath)

              return {
                mutatedVariable: element.lhsName,
                mutatedType: element.operation,
                literalValue: literalValue,
              };
            }
          );

          setSelectedNodeData(processedData);
        },
        nop_def: () => {
          setSelectedNodeData(selectedNode?.outgoingEdges);
        },
        startThread: () => {

        },
      };

      if (props.internalComponent) {
        const complexData =
          processComplexData[
            props.internalComponent as keyof typeof processComplexData
          ];

        if (complexData instanceof Function) complexData();
      }
    }
  }, [
    props.data,
    rawData,
    wfRunRawData,
    selectedNode,
    props.nodeName,
    props.internalComponent,
    threadName,
  ]);

  useEffect( () => {
    if(!props?.nodeName) return 
    setType(props.nodeName.split('-').reverse()[0])
  },[props.nodeName])

  const setToggleSideBar = (
    value: boolean,
    isError: boolean,
    code: string,
    language?: string
  ) => {
    props.setToggleSideBar(value);
    if (
      language === undefined ||
      language === "jsonObj" ||
      language === "jsonArr"
    ) {
      props.setCode(JSON.parse(code));
      return;
    }
    props.setCode(code);
    props.setLanguage(language);
    props.setError(isError);
  };

  return (
    <div className="drawer-component">
      <>
        <div className="drawer__threadSelector">
          <p className="drawer__threadSelector__header">THREADSPEC NAME</p>
          <div className="drawer__threadSelector__container">
            <select
              className="drawer__threadSelector__container__select"
              value={threadName}
              onChange={(event) => setThreadHandler(event.target.value)}
            >
              {props.data &&
                Object.keys(props.data.threadSpecs).map((name, index) => {
                  return (
                    <option key={index} value={name}>
                      {name}
                    </option>
                  );
                })}
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
          {mainData &&
            mainData.map(({ name, type }, index) => {
              return (
                <div key={index}>
                  <div key={index} className={`grid-2 `}>
                    <p className="center">{name}</p>
                    <p className="center">{type}</p>
                  </div>
                </div>
              );
            })}
        </div>

        {props.internalComponent === "task" && (
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
        )}
        {props.internalComponent === "nop_def" && (
          <NOPInformation
            {...{
              data: selectedNodeData,
              nodeName: props.nodeName || '',
              errorData: errorData,
              setToggleSideBar: setToggleSideBar,
            }}
          />
        )}
        {props.internalComponent === "externalEvent" && (
          <ExternalEventInformation
            {...{
              isWFRun : false,
              data: props.datao.find((d : any) => d.name === props.nodeName),
              nodeName: props.nodeName,
              errorData: errorData,
              setToggleSideBar: setToggleSideBar,
            }}
          />
        )}
        {props.internalComponent === "startThread" && (
          <SpawnChildInformation
            {...{
              linkedThread: setThreadHandler,
              nodeName: props.nodeName,
              errorData: errorData,
              wfRunData: wfRunData,
              setToggleSideBar: setToggleSideBar,
            }}
          />
        )}
        {props.internalComponent === "waitForThread" && (
          <WaitChildInformation
            {...{
              linkedThread: setThreadHandler,
              nodeName: props.nodeName  || '' ,
              errorData: errorData,
              wfRunDrawer: false,
              setToggleSideBar: setToggleSideBar,
            }}
          />
        )}
        {type === 'SLEEP' ? <SleepNodeInformation isWFRun={false} data={props.datao.find((d : any) => d.name === props.nodeName)} /> : ''}

      </>
    </div>
  );
};
