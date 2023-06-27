import Link from "next/link"
import { Result } from "../../sections/MetadataSearch"

interface Props {
    results?:Result[]
}
export const MetadataSearchTable = ({results}:Props) => {

    return <article className="flex-1">
        { results && <table>
            <tbody>
                {results.map( (r:Result, ix:number) => <tr key={ix}>
                    <td>
                        {(r.type==='WfSpec') ? <Link href={`/wfspec/${r.name}/${r.version}`}>{r.name}</Link>  :  undefined}
                        {(r.type==='TaskDef') ? <Link href={`/taskdef/${r.name}`}>{r.name}</Link>  :  undefined}
                        {(r.type==='ExternalEventDef') ? r.name :  undefined}
                    </td>
                    <td>{r.version}</td>
                    <td>{r.type}</td>
                </tr>)}
            </tbody>
        </table> }
    </article>
}

