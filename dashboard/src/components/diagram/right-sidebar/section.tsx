import { ReactNode, Children, useState } from 'react'

interface SectionProps {
  title: ReactNode
  children?: ReactNode
  isCollapsedDefault?: boolean
}

export function Section({ title, children, isCollapsedDefault = false }: SectionProps) {
  const [isCollapsed, setIsCollapsed] = useState(isCollapsedDefault)

  const toggleCollapsed = () => {
    setIsCollapsed(!isCollapsed)
  }

  return (
    <div className="mt-2 w-full rounded-md border border-gray-200 bg-gray-50">
      <button
        onClick={toggleCollapsed}
        className="flex w-full items-center justify-between rounded-t-md p-2 text-xs font-medium transition-colors hover:bg-gray-100"
      >
        <span className="text-ellipsis overflow-hidden">{title}</span>
        <svg
          width="12"
          height="12"
          fill="none"
          viewBox="0 0 24 24"
          className={`transition-transform duration-200 ${isCollapsed ? 'rotate-90' : '-rotate-90'}`}
        >
          <path d="M9 18l6-6-6-6" stroke="#656565" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </button>
      <div
        className={`overflow-hidden transition-all duration-300 ease-in-out ${isCollapsed ? 'max-h-0 opacity-0' : 'max-h-96 opacity-100'
          }`}
      >
        <div className="space-y-1 text-xs p-2">
          {Children.count(children) === 0 && <div className="text-gray-500">No data</div>}
          {children}
        </div>
      </div>
    </div>
  )
}
