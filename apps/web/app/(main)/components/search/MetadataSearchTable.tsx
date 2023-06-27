import Link from "next/link"
import { Result } from "../../sections/MetadataSearch"


interface Props {
    results?:Result[]
}
export const MetadataSearchTable = ({results}:Props) => {

    return <div className="table">
        { results && <table className="flex-1" style={{width:"100%"}}>
            <thead className="flex" style={{
                width:"100%"
            }}>
                <tr className="flex w-full">
                    <th className="w-full ">NAME / ID</th>
                    <th className="w-full text-center">TYPE</th>
                    <th className="w-full text-center">VERSION</th>
                </tr>
            </thead>
            <tbody>
                {results.map( (r:Result, ix:number) => <tr key={ix} className="flex w-full">
                    <td className="capitalize">
                        {(r.type==='WfSpec') ? <Link href={`/wfspec/${r.name}/${r.version}`}>{r.name}</Link>  :  undefined}
                        {(r.type==='TaskDef') ? <Link href={`/taskdef/${r.name}`}>{r.name}</Link>  :  undefined}
                        {(r.type==='ExternalEventDef') ? r.name :  undefined}
                    </td>
                    <td className="text-center ">{r.type}</td>
                    <td className="text-center "> {r.version}</td>
                </tr>)}
            </tbody>
        </table> }
    </div>
}

