import Link from 'next/link'
import type { Result } from '../../sections/WfRunSearch'

interface WfRunSearchTableProps {
    wfspec: string
    results?: Result[]
}
export function WfRunSearchTable({ results }: WfRunSearchTableProps) {
    return (
        <div className='table'>
            {results ? <table className='flex-1' style={{ width: '100%' }}>
                <thead
                    className='flex'
                    style={{
                        width: '100%'
                    }}
                >
                    <tr className='flex w-full'>
                        <th className='w-full '>NAME / ID</th>
                        <th className='w-full text-center'>STATUS</th>
                    </tr>
                </thead>
                <tbody
                    className='scrollbar'
                    style={{
                        height: '519px'
                    }}
                >
                    {results.map((r: Result) => (
                        <tr className='flex w-full' key={r.id}>
                            <td>
                                <Link href={`/wfrun/${r.id}`}>{r.id}</Link>
                            </td>
                            <td className='text-center '> {r.status}</td>
                        </tr>
                    ))}
                </tbody>
            </table> : null}
        </div>
    )
}
