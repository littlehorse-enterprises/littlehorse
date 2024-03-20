import { Navigation } from '@/app/(authenticated)/components/Navigation'
import { TaskDef as TaskDefProto } from 'littlehorse-client/dist/proto/task_def'
import { FC } from 'react'
import { Details } from './Details'
import { InputVars } from './InputVars'

type Props = {
  spec: TaskDefProto
}
export const TaskDef: FC<Props> = ({ spec }) => {
  return (
    <>
      <Navigation href="/?type=taskDef" title="Go back to TaskDefs" />
      <Details id={spec.id} />
      <InputVars inputVars={spec.inputVars} />
    </>
  )
}
