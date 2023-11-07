import Link from 'next/link'
import type { Result } from '../sections/userTaskRunSearch'

interface Props {
    wfspec: string;
    results?: Result[];
}
export function UserTaskRunSearchTable({ wfspec, results }: Props) {
  return (
    <div className="table">
      {results ? <table className="flex-1" style={{ width: '100%' }}>
        <thead
          className="flex"
          style={{
            width: '100%',
          }}
        >
          <tr className="flex w-full">
            <th className="w-full ">NAME / ID</th>
            <th className='w-full'>GUID</th>
            <th className="w-full text-center">STATUS</th>
          </tr>
        </thead>
        <tbody
          className="scrollbar"
          style={{
            height: '519px',
          }}
        >
          {results.map((r: Result, ix: number) => (
            <tr className="flex w-full" key={ix}>
              <td>
                <Link href={`/wfrun/${r?.wfRunId}`}>
                  {r?.wfRunId}
                </Link>
              </td>
              <td className=" "> {r?.userTaskGuid}</td>
              <td className="text-center "> {r?.status}</td>
            </tr>
          ))}
        </tbody>
      </table> : null}
    </div>
  )
}
