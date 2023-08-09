import React, { use, useEffect, useState } from "react";
import { parseKey } from "./internals/drawerInternals";
import {
  FailureInformation,
  LH_EXCEPTION,
} from "./internals/FailureInformation";

export type Value =
  | {
      type: "STR";
      str: string;
    }
  | {
      type: "JSON_OBJ";
      jsonObj: string;
    }
  | {
      type: "DOUBLE";
      double: number;
    }
  | {
      type: "BOOL";
      bool: boolean;
    }
  | {
      type: "INT";
      int: string;
    };

interface MainDataItem {
  wfRunId?: string;
  RunNumber: string;
  name: string;
  index: number;
  run: any;
  type?: string;
}
interface WfVariableProps extends MainDataItem {
  setToggleSideBar: (
    value: boolean,
    isError: boolean,
    code: string,
    language?: string
  ) => void;
  errorData: {
    handlerSpecName: string;
    exception: LH_EXCEPTION | string;
  }[];
}

const WfVariable = (props: WfVariableProps) => {
  const [wfVariable, setVariable] = useState<Value | undefined>();
  const [processVal, setProcessVal] = useState<
    string | number | boolean | undefined
  >();
  const [jsonObjClass, setJsonObjClass] = useState("");

  const getVariableData = async (wfRunId, RunNumber, name) => {
    const res = await fetch("/api/drawer/variable", {
      method: "POST",
      body: JSON.stringify({
        wfRunId,
        RunNumber,
        name,
      }),
    });

    if (res.ok) {
      const content = await res.json();
      setVariable(content.result?.value);
    }
  };

  function processValue(value: Value | undefined) {
    if (value === null || value === undefined) return "NULL";

    switch (value.type) {
      case "STR":
        return value.str;
      case "JSON_OBJ":
        return value.jsonObj;
      case "DOUBLE":
        return value.double;
      case "BOOL":
        return value.bool;
      case "INT":
        return value.int;
      default:
        return "NULL";
    }
  }
  const onJsonObjClick = () => {
    if (wfVariable?.type === "JSON_OBJ") {
      props.setToggleSideBar(true, false, wfVariable.jsonObj, "json");
    }
  };

  const onParseError = (data: any) => {
    if (typeof data === "string") {
      props.setToggleSideBar(true, true, data, "str");
      return;
    }
    const key = parseKey(data.type.toLowerCase());
    const error = data[key];
    props.setToggleSideBar(true, true, error, key);
  };

  useEffect(() => {
    if (wfVariable !== undefined) {
      const processVal = processValue(wfVariable);
      setProcessVal(processVal);
      if (wfVariable.type === "JSON_OBJ") {
        setJsonObjClass("drawer__mainTable__clickable");
      } else {
        setJsonObjClass(""); // Reset class if not JSON_OBJ
      }
    } else {
      getVariableData(props.wfRunId || "", props.RunNumber, props.name);
    }
  }, [wfVariable]);

  return (
    <>
      <div key={props.index} className={`grid-2 ${props.run ? "grid-3" : ""}`}>
        <p className="center">{props.name}</p>
        <p className="center">{props.type}</p>
        {props.run && (
          <p
            className={`${
              jsonObjClass && wfVariable?.type === "JSON_OBJ"
                ? "json-text-collapsed drawer__mainTable__clickable"
                : "center"
            } json-text-collapsed`}
            onClick={onJsonObjClick}
          >
            {processVal}
          </p>
        )}
      </div>
      <FailureInformation data={props.errorData} openError={onParseError} />
    </>
  );
};
export default WfVariable;
