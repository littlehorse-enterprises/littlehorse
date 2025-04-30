import { LHStatus } from 'littlehorse-client/proto'
import { FC, PropsWithChildren } from 'react'
import { StatusPin } from './StatusPin'

type Props = PropsWithChildren<{ isFaded?: boolean; status?: LHStatus }>
export const Fade: FC<Props> = ({ isFaded, status, children }) => {
  return (
    <div className="relative">
      <StatusPin status={status} />
      <div className={'relative ' + (isFaded ? 'opacity-25' : 'opacity-100')}>{children}</div>
    </div>
  )
}
