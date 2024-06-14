import { LHStatus } from 'littlehorse-client'
import { FC, PropsWithChildren } from 'react'
import { StatusPin } from './StatusPin'

type Props = PropsWithChildren<{ fade?: boolean; status?: LHStatus }>
export const Fade: FC<Props> = ({ fade, status, children }) => {
  return (
    <div className="relative">
      <StatusPin status={status} />
      <div className={'relative ' + (fade ? 'opacity-25' : 'opacity-100')}>{children}</div>
    </div>
  )
}
