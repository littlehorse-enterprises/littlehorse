import { VARIABLE_TYPES } from '@/app/constants'
import { TaskDef } from 'littlehorse-client/proto'
import { FC } from 'react'

type Props = Pick<TaskDef, 'inputVars'>
export const InputVars: FC<Props> = ({ inputVars }) => {
  if (inputVars.length === 0) return <div className="italic">No input variables</div>

  return (
    <div className="">
      <h2 className="mb-2 text-lg font-bold">Input Variables</h2>
      {inputVars.map(({ name, type, defaultValue }) => (
        <div key={name} className="mb-1 flex items-center gap-1">
          <div className="rounded bg-gray-100 px-2 py-1 font-mono text-fuchsia-500">{name}</div>
          <div className="rounded bg-yellow-100 p-1 text-xs">{VARIABLE_TYPES[type]}</div>
          {defaultValue && <div className="">{Object.values(defaultValue)[0]}</div>}
        </div>
      ))}
    </div>
  )
}
