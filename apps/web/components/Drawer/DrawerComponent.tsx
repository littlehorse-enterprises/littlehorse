import React, { useEffect, useState } from "react";
import os from "os";
import { TaskDefInformation } from "./internals/TaskDefInformation";
import { NOPInformation } from "./internals/NOPInformation";
import { ExternalEventInformation } from "./internals/ExternalEventInformation";
import { SpawnChildInformation } from "./internals/SpawnChildInformation";
import { WaitChildInformation } from "./internals/WaitChildInformation";
import { LH_EXCEPTION } from "./internals/FailureInformation";
import { parseKey } from "./internals/drawerInternals";

interface DrawerComponentProps {
  internalComponent?: string | undefined;
  data: any;
  nodeName: string;
  wfRunId?: string;
  setToggleSideBar: (value: boolean) => void;
  setCode: (code: string) => void;
  setLanguage: (language: string) => void;
  setError: (value: boolean) => void;
  run?: any;
}

export const DrawerComponent = (props: DrawerComponentProps) => {
  const [rawData, setRawData] = useState<any>();
  const [wfRunRawData, setWfRunRawData] = useState<any>();
  const [selectedNodeData, setSelectedNodeData] = useState<any>();
  const [mainData, setMainData] = useState<{ name: string; type: string }[]>();
  const [selectedNode, setSelectedNode] = useState<any>();
  const [wfRunData, setWfRunData] = useState<any>();
  const [errorData, setErrorData] = useState<any>([]);
  const [lastSelectedNode, setLastSelectedNode] = useState<any>();
  const [threadName, setThreadName] = useState<string>();

  const changeThread = () => {
    if (props.data.threadSpecs) {
      const keys = Object.keys(props.data.threadSpecs);

      setThreadName(keys[1]);
    }
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

  const getErrorData: any = (node: any, key: string) => {
    if (node) {
      const logs = "task" in node ? node.task.logOutput : null;
      const data = node[key].map((element: any) => {
        return {
          ...element,
          log: logs,
        };
      });

      setErrorData(data);
    } else {
      setErrorData([]);
    }
  };

  useEffect(() => {
    if (props.wfRunId && wfRunRawData === undefined)
      getData(
        "../../api/drawer/wfRun/",
        props.wfRunId,
        setWfRunRawData,
        "results"
      );

    if (props.data) {
      if (mainData === undefined || threadName) {
        let selectedThread;

        if (threadName === undefined) {
          selectedThread = props.data.entrypointThreadName;
          setThreadName(selectedThread);
        }

        setMainData(
          props.data.threadSpecs[selectedThread || threadName].variableDefs
        );
      }

      if (props.nodeName !== lastSelectedNode) {
        setLastSelectedNode(props.nodeName);
        setSelectedNodeData(undefined);
        setWfRunData(undefined);
        setSelectedNode(
          props.data.threadSpecs.entrypoint.nodes[props.nodeName]
        );
        if (props.wfRunId) {
          const wfRunNode = wfRunRawData.find(
            (element: any) => element.nodeName === props.nodeName
          );
          if (wfRunNode) {
            getErrorData(wfRunNode, "failures");
          } else {
            setErrorData([]);
          }
        } else {
          getErrorData(
            props.data.threadSpecs.entrypoint.nodes[props.nodeName],
            "failureHandlers"
          );
        }
      }

      const processComplexData = {
        task: () => {
          if (rawData === undefined)
            getData(
              "../../api/drawer/taskDef/",
              selectedNode.task.taskDefName,
              setRawData,
              "result"
            );
          else {
            const processedData = rawData.inputVars.map(
              (
                element: { name: string; type: string; _: any },
                index: number
              ) => {
                const currentVariable = selectedNode.task.variables[index];

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

            if (props.wfRunId && wfRunRawData) {
              const wfRunNode = wfRunRawData.find(
                (element: any) => element.nodeName === props.nodeName
              );

              if (wfRunNode && props.internalComponent) {
                const data =
                  wfRunNode[props.internalComponent as keyof typeof wfRunNode];
                if (data) {
                  const inputs = (data as any).inputVariables.map(
                    (element: any, index: number) => {
                      const variableType: string = element.value.type;
                      const correctKey = parseKey(variableType) || "";
                      const value = element.value[correctKey] || "";

                      processedData[index].value = value;

                      return {
                        name: element.varName,
                        type: element.value.type,
                        value: value,
                      };
                    }
                  );

                  const outputs = (data as any).inputVariables.map(
                    (element: any) => {
                      const variableType: string = element.value.type;
                      const correctKey = parseKey(variableType) || "";
                      const value = element.value[correctKey] || "";

                      return {
                        type: element.value.type,
                        value: value,
                      };
                    }
                  );

                  const wfRunComplexData = {
                    nodeData: {
                      reachTime: wfRunNode.arrivalTime || "",
                      completionTime: wfRunNode.endTime || "",
                      status: wfRunNode.status,
                    },
                    inputs: inputs,
                    outputs: outputs,
                  };

                  setWfRunData(wfRunComplexData);
                }
              }
            }

            setSelectedNodeData(processedData);
          }
        },
        externalEvent: () => {
          if (props.wfRunId && wfRunRawData) {
            const wfRunNode = wfRunRawData.find(
              (element: any) => element.nodeName === props.nodeName
            );

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
              };

              if (data) {
                const wfRunComplexData = {
                  nodeData: {
                    reachTime: wfRunNode.arrivalTime || "",
                    completionTime: wfRunNode.endTime || "",
                    status: wfRunNode.status,
                  },
                  guid: data.externalEventId?.guid || "",
                  arrivedTime: data?.eventTime || "",
                  arrived: data.eventTime ? "YES" : "NO",
                };

                setWfRunData(wfRunComplexData);
              }
            }
          }

          const processedData = selectedNode?.variableMutations.map(
            (element: {
              lhsName: string;
              operation: string;
              nodeOutput: any;
            }) => {
              let literalValue =
                selectedNode.externalEvent.externalEventDefName;

              if (element.nodeOutput.hasOwnProperty("jsonpath"))
                console.warn("Missing fix of property: jsonpath");

              if (element.nodeOutput.hasOwnProperty("jsonPath"))
                console.warn(
                  "Property fixed: jsonPath; NEED TO SUBSTITUTE ON CODE"
                );

              //TODO: verify that the jsonPath property is right spelled
              const jsonPath = element.nodeOutput.jsonpath?.replace("$", "");

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
          if (props.wfRunId && wfRunRawData) {
            const wfRunNode = wfRunRawData.find(
              (element: any) => element.nodeName === props.nodeName
            );

            console.log("wfRunRawData", wfRunRawData);
            console.log("wfRunNode", wfRunNode);
            console.log("props.nodeName", props.nodeName);

            if (wfRunNode && props.internalComponent) {
              const data = wfRunNode[
                props.internalComponent as keyof typeof wfRunNode
              ] as {
                externalEventDefName: string;
                eventTime: string;
              };

              if (data) {
                const wfRunComplexData = {
                  nodeData: {
                    reachTime: wfRunNode.arrivalTime || "",
                    completionTime: wfRunNode.endTime || "",
                    status: wfRunNode.status,
                  },
                };

                console.log("wfRunComplexData", wfRunComplexData);

                setWfRunData(wfRunComplexData);
              }
            }
          }
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
              onChange={(event) => setThreadName(event.target.value)}
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
          <div className="drawer__mainTable__header">ThreadRun Variables</div>
          <div className="drawer__mainTable__header__subheaders">
            <p className="center ">NAME</p>
            <p className="center">TYPE</p>
          </div>
          {mainData &&
            mainData.map(({ name, type }, index) => {
              return (
                <div key={index} className="grid-2">
                  <p className="center">{name}</p>
                  <p className="center">{type}</p>
                </div>
              );
            })}
        </div>
        {props.internalComponent === "task" && (
          <TaskDefInformation
            {...{
              linkedThread: changeThread,
              data: selectedNodeData,
              nodeName: props.nodeName,
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
              nodeName: props.nodeName,
              errorData: errorData,
              setToggleSideBar: setToggleSideBar,
            }}
          />
        )}
        {props.internalComponent === "externalEvent" && (
          <ExternalEventInformation
            {...{
              data: selectedNodeData,
              nodeName: props.nodeName,
              errorData: errorData,
              wfRunData: wfRunData,
              setToggleSideBar: setToggleSideBar,
            }}
          />
        )}
        {props.internalComponent === "startThread" && (
          <SpawnChildInformation
            {...{
              linkedThread: changeThread,
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
              linkedThread: changeThread,
              nodeName: props.nodeName,
              errorData: errorData,
              wfRunDrawer: props.wfRunId === undefined ? false : true,
              setToggleSideBar: setToggleSideBar,
            }}
          />
        )}
      </>
    </div>
  );
};
