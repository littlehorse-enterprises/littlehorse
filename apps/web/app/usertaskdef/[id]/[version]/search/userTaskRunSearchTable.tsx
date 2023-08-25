import Link from "next/link";
import { Result } from "../sections/userTaskRunSearch";

interface Props {
    wfspec: string;
    results?: Result[];
}
export const UserTaskRunSearchTable = ({ wfspec, results }: Props) => {
    console.log("resultss", results);
    return (
        <div className="table">
            {results && (
                <table className="flex-1" style={{ width: "100%" }}>
                    <thead
                        className="flex"
                        style={{
                            width: "100%",
                        }}
                    >
                        <tr className="flex w-full">
                            <th className="w-full ">NAME / ID</th>
                            <th className="w-full text-center">STATUS</th>
                            <th className="w-full text-center">VERSION</th>
                        </tr>
                    </thead>
                    <tbody
                        className="scrollbar"
                        style={{
                            height: "519px",
                        }}
                    >
                        {results.map((r: Result, ix: number) => (
                            <tr key={ix} className="flex w-full">
                                <td>
                                    <Link href={`/wfrun/${"x"}/${r?.wfRunId}`}>
                                        {r?.wfRunId}
                                    </Link>
                                </td>
                                <td className="text-center "> {r?.status}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
};
