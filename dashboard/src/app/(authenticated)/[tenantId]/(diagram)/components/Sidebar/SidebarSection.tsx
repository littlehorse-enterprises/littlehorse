import { ChevronDown, ChevronUp, FileSliders } from 'lucide-react'
import { FC, MouseEvent, PropsWithChildren, useCallback, useState } from 'react'

type Props = PropsWithChildren & {
  title: string
  collapsed?: boolean
  level?: number
  collapsible?: boolean
  icon: React.ElementType
}
export const SidebarSection: FC<Props> = ({ children, title, icon, collapsible = false, collapsed = false, level = 0 }) => {
  const [open, setOpen] = useState(collapsed)

  const IconSymbol = open ? ChevronDown : ChevronUp
  const Icon = icon

  const handleOpen = useCallback(() => {
    if (collapsible) {
      setOpen(!open)
    }
  }, [collapsible, open, setOpen])

  return (
    <div className={`flex flex-col pl-${level * 4} ${level > 0 ? 'border-l border-gray-700 ml-2' : ''} pb-2`}>
      <div className="flex cursor-pointer items-center gap-2 pt-2" onClick={handleOpen}>
        {collapsible && <IconSymbol className="h-4 w-4 flex-none" />}
        <Icon className="h-4 w-4 flex-none" />
        <h3 className="grow text-sm font-bold">{title}</h3>
      </div>
      <div className="">{(open || !collapsible) && children}</div>
    </div>
  )
}
