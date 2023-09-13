"use client";
import { useEffect, useState } from "react";
import Loading from "ui/components/Loading";
import { InputVarsTaskDef } from "../../../../../interfaces/InputVarsTaskDef";
import { TaskDefInformationResponse } from "../../../../../interfaces/TaskDefInformationResponse";
import { VersionChanger } from "../components/VersionChanger";

export const UserTaskDefSchemaInformation = ({
    id,
    version,
}: {
    id: string;
    version: string;
}) => {
    const [loadingInputVars, setLoadingInputVars] = useState(true);
    const [inputVars, setInputVars] = useState<InputVarsTaskDef[]>([]);
    const [fieldsInputs, setFieldsInputs] = useState<any[]>([]);

    const taskInformation = async () => {
        const res = await fetch("/api/information/userTaskDef", {
            method: "POST",
            body: JSON.stringify({
                id,
                version,
            }),
        });
        if (res.ok) {
            const data: any = await res.json();
            setFieldsInputs(data.result.fields);
            setLoadingInputVars(false);
            console.log("Input Variables usertaskdef:", data.result.fields);
        }
    };

    useEffect(() => {
        taskInformation();
    }, []);

    return (
        <section>
            <div className="between">
                <h2>UserTaskDef Schema Information</h2>
                <VersionChanger version={version} id={id} />
            </div>

            <div className="table">
                <table className="flex-1" style={{ width: "100%" }}>
                    <caption>Input Variables</caption>
                    <thead
                        className="flex"
                        style={{
                            width: "100%",
                        }}
                    >
                        <tr className="flex w-full">
                            <th className="w-full text-center">NAME</th>
                            <th className="w-full text-center">DISPLAY NAME</th>
                            <th className="w-full text-center">TYPE</th>
                        </tr>
                    </thead>
                    <tbody className="no-scrollbar">
                        {fieldsInputs.length ? (
                            fieldsInputs.map((fields, index, row) => (
                                <tr
                                    className="flex w-full"
                                    key={`${fields.name}-${index}`}
                                >
                                    <td className="w-full text-center">
                                        {fields.name}
                                    </td>
                                    <td className="w-full text-center">
                                        {fields.displayName}
                                    </td>
                                    <td className="w-full text-center">
                                        {fields.type}
                                    </td>
                                </tr>
                            ))
                        ) : loadingInputVars ? (
                            <Loading style={{ padding: "20px" }} />
                        ) : (
                            <div
                                className="flex items-center justify-center"
                                style={{ padding: "20px" }}
                            >
                                No variables required
                            </div>
                        )}
                    </tbody>
                </table>
            </div>
        </section>
    );
};
