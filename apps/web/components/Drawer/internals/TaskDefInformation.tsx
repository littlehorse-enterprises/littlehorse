import React, { useEffect, useState } from "react";
import Image from "next/image";
import linkSvg from "./link.svg";
import correctArrowSvg from "./correct-arrow.svg";
import { FailureInformation, LH_EXCEPTION } from "./FailureInformation";
import { NodeData, NodeDataProps } from "./NodeData";
import { parseKey } from "./drawerInternals";
import Link from "next/link";
import { Value } from "../wfVariable";

interface TaskDefInformationProps {
  linkedThread: (string) => void;
  data:
    | { name: string; type: string; variableName: string; value?: string }[]
    | undefined;
  nodeName: string;
  errorData: {
    handlerSpecName: string;
    exception: LH_EXCEPTION | string;
  }[];
  wfRunData?: {
    nodeData: NodeDataProps;
    inputs: {
      name: string;
      type: string;
      value: string;
    }[];
    outputs: {
      type: string;
      value: string;
    }[];
  };
  setToggleSideBar: (
    value: boolean,
    isError: boolean,
    code: string,
    language?: string
  ) => void;
}

export const TaskDefInformation = (props: TaskDefInformationProps) => {
  const onParseError = (data: any) => {
    if (typeof data == "string") {
      props.setToggleSideBar(true, true, data, "str");
      return;
    }
    const key = parseKey(data.type.toLowerCase());
    const error = data[key];
    props.setToggleSideBar(true, true, error, key);
  };

  const LinkToSnipper = (value: any) => (
    <button
      className="btn btn-wfrun-link"
      onClick={(e) => {
        props.setToggleSideBar(true, false, value.data);
      }}
    >
      See More
    </button>
  );

  return (
    <>
      <div className="component-header">
        <Image
          src={correctArrowSvg}
          alt={"correct-arrow"}
          width={24}
          height={24}
        />
        <div>
          <p>Task Node Information</p>
          <p className="component-header__subheader">{props.nodeName}</p>
        </div>
      </div>
      {props.wfRunData && <NodeData {...props.wfRunData.nodeData} />}
      <div className="drawer__task__table">
        <div className="drawer__task__table__header">Input Variables</div>
        <div
          className={`drawer__task__table__header__subheaders ${
            props.wfRunData ? "grid-3" : "grid-3"
          }`}
        >
          <p className="drawer__task__table__header__subheaders center">
            TaskDef
            <br />
            Variable Name
          </p>
          <p className="drawer__task__table__header__subheaders center">
            TaskDef
            <br />
            Variable Type
          </p>
          {!props.wfRunData && (
            <p className="drawer__task__table__header__subheaders center">
              Workflow
              <br />
              Variable
            </p>
          )}
          {props.wfRunData && <p className="center">Value</p>}
        </div>
        {props.data &&
          props.data.map(
            ({ name, type, variableName, value }, index: number) => {
              let link;
              if (type === "JSON_OBJ" || type === "JSON_ARR") {
                link = <LinkToSnipper data={value} />;
              }
              if (props.wfRunData)
                return (
                  <div key={index} className="grid-3">
                    <p className="center">{name}</p>
                    <p className="center">{type}</p>
                    <p className="center">{link ? link : value}</p>
                  </div>
                );
              else
                return (
                  <div key={index} className="grid-3">
                    <p className="center">{name}</p>
                    <p className="center">{type}</p>
                    <p className="center">{variableName}</p>
                  </div>
                );
            }
          )}
      </div>
      {props.wfRunData && (
        <>
          <div className="drawer__task__wfrun-outputs">
            <div className="drawer__task__wfrun-outputs__label">Outputs</div>
            <div className="drawer__task__wfrun-outputs__header">
              <p className="center">TYPE</p>
              <p className="center">VALUE</p>
            </div>
            {props.wfRunData.outputs &&
              props.wfRunData.outputs.map(({ type, value }, index: number) => {
                let link;
                if (type === "JSON_OBJ" || type === "JSON_ARR") {
                  link = <LinkToSnipper data={value} />;
                }
                return (
                  <div key={index} className="grid-2">
                    <p className="center">{type}</p>
                    <p className="center">{link ? link : value}</p>
                  </div>
                );
              })}
          </div>
        </>
      )}
      <div className="drawer__task__link">
        <div className="drawer__task__link__title">TaskDef linked</div>
        <div className="drawer__task__link__container">
          <Link
            href={
              "/taskdef/" + props?.nodeName.split("-").slice(1, -1).join("-")
            }
            className="drawer__task__link__container__clickable"
            style={{
              textDecoration: "none",
            }}
          >
            <Image src={linkSvg} alt={"link"} width={20} height={10} />
            <p className="drawer__task__link__container__clickable__text">
              {props?.nodeName.split("-").slice(1, -1).join("-") || ""}
            </p>
          </Link>
        </div>
      </div>
      <FailureInformation data={props.errorData} openError={onParseError} />
    </>
  );
};
