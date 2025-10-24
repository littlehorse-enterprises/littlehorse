import { FC } from 'react'
type Props = {
  label: string
  content?: string
}
export const LabelContent: FC<Props> = ({ label, content }) => {
  return (
    <div className="mb-2">
      <p className="text-[0.75em] text-slate-400">{label}</p>
      {content && <p className="text-lg font-medium">{content}</p>}
    </div>
  )
}
