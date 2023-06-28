import Link from "next/link"
import { Result } from "../../sections/WfRunSearch"


interface Props {
    results?:Result[]
}
export const WfRunSearchTable = ({results}:Props) => {

    return <div className="table">
    { results && <table className="flex-1" style={{width:"100%"}}>
        <thead className="flex" style={{
            width:"100%"
        }}>
            <tr className="flex w-full">
                <th className="w-full ">NAME / ID</th>
                <th className="w-full text-center">STATUS</th>
            </tr>
        </thead>
        <tbody style={{
            height: "519px"
        }}>
            {results.map( (r:Result, ix:number) => <tr key={ix} className="flex w-full">
                <td ><Link href={`/wfrun/${r.id}`}>{r.id}</Link></td>
                <td className="text-center "> {r.status}</td>
            </tr>)}
        </tbody>
    </table> }
</div>

}

