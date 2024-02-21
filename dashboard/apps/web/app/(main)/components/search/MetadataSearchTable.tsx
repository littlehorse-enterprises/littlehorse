import Link from 'next/link'
import type { Result } from '../../sections/MetadataSearch'


interface MetadataSearchTableProps {
    results :Result[]
}
export function MetadataSearchTable({ results }: MetadataSearchTableProps) {

    if (results.length === 0) {
        return (<div className="flex items-center justify-items-center justify-center text-center flex-1">
                    <p>No data available</p>
                </div>)
    }

    return <div className="table flex-1">
        { results ? <table className="flex-1" style={{ width:'100%' }}>
            <thead className="flex" style={{
                width:'100%'
            }}>
                <tr className="flex w-full">
                    <th className="w-full ">NAME / ID</th>
                    <th className="w-full text-center">TYPE</th>
                    <th className="w-full text-center">VERSION</th>
                </tr>
            </thead>
            <tbody className="scrollbar" style={{
                height: '519px'
            }}>
                {/* eslint-disable-next-line react/no-array-index-key */}
                {results.map( (r:Result) => <tr className="flex w-full" key={r.name + r.version}>
                    <td className="capitalize">
                        {(r.type==='WfSpec') ? <Link href={`/wfspec/${r.name}/${r.version}`}>{r.name}</Link>  :  undefined}
                        {(r.type==='TaskDef') ? <Link href={`/taskdef/${r.name}`}>{r.name}</Link>  :  undefined}
                        {(r.type==='UserTaskDef') ? <Link href={`/usertaskdef/${r.name}/${r.version}`}>{r.name}</Link>  :  undefined}
                        {(r.type==='ExternalEventDef') ? r.name :  undefined}
                    </td>
                    <td className="text-center ">{r.type}</td>
                    <td className="text-center "> {r.version}</td>
                </tr>)}
            </tbody>
        </table> : null }
    </div>
}
