import { VARIABLE_TYPES } from '@/app/constants'
import { TaskDef } from 'littlehorse-client/dist/proto/task_def'
import { FC } from 'react'

type Props = Pick<TaskDef, 'inputVars'>
export const InputVars: FC<Props> = ({ inputVars }) => {
  if (inputVars.length === 0) return <div className="italic">No input variables</div>

  return (
    <div className="">
      <h2 className="font-bold text-lg mb-2">Input Variables</h2>
      {inputVars.map(({ name, type, defaultValue }) => (
        <div key={name} className="flex items-center gap-1 mb-1">
          <div className="text-fuchsia-500 font-mono bg-gray-100 rounded py-1 px-2">{name}</div>
          <div className="text-xs bg-yellow-100 rounded p-1">{VARIABLE_TYPES[type]}</div>
          {defaultValue && <div className="">{Object.values(defaultValue)[0]}</div>}
        </div>
      ))}
    </div>
  )
}
