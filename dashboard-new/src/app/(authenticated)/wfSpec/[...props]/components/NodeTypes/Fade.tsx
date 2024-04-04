import { FC, PropsWithChildren } from 'react'

type Props = PropsWithChildren<{ fade?: boolean }>
export const Fade: FC<Props> = ({ fade, children }) => {
  return <div className={fade ? 'opacity-25' : 'opacity-100'}>{children}</div>
}
