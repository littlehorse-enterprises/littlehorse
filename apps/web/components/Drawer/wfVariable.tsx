import React, { use, useEffect, useState } from "react";

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

const WfVariable = (props: MainDataItem) => {
  const [wfVariable, setVariable] = useState<Value | undefined>();
  const [processVal, setProcessVal] = useState<
    string | number | boolean | undefined
  >();

  const getVariableData = async (wfRunId, RunNumber, name) => {
    const res = await fetch("/api/drawer/variable", {
      method: "POST",
      body: JSON.stringify({
        wfRunId,
        RunNumber,
        name,
      }),
    });

    console.log("variableresss", res);
    if (res.ok) {
      const content = await res.json();
      setVariable(content.result?.value);
      console.log("variable", content.result?.value);
    }
  };
  function processValue(value: Value | undefined) {
    console.log("textovalue", value);
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
  useEffect(() => {
    if (wfVariable !== undefined) {
      const processVal = processValue(wfVariable);
      setProcessVal(processVal);
    } else {
      getVariableData(props.wfRunId || "", props.RunNumber, props.name);
    }
  }, [wfVariable]);

  return (
    <div key={props.index} className={`grid-2 ${props.run ? "grid-3" : ""}`}>
      <p className="center">{props.name}</p>
      <p className="center">{props.type}</p>

      {props.run && <p className="center">{processVal}</p>}
    </div>
  );
};
export default WfVariable;
