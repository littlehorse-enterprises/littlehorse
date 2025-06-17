import { ReactNode } from 'react'

interface BaseNodeComponentProps {
  title: string
  type: string
  description?: string
  children?: ReactNode
  additionalSections?: {
    title: string
    content: ReactNode
  }[]
}

export function BaseNodeComponent({ title, type, description, children, additionalSections }: BaseNodeComponentProps) {
  return (
    <div className="space-y-4">
      <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
        <h4 className="mb-2 text-xs font-medium">{title}</h4>
        <div className="space-y-1 text-xs">
          <div className="flex justify-between">
            <span className="text-[#656565]">Type:</span>
            <span className="font-mono">{type}</span>
          </div>
          {description && (
            <div className="text-[#656565] text-xs">
              {description}
            </div>
          )}
          {children}
        </div>
      </div>

      {additionalSections?.map((section, index) => (
        <div key={index} className="rounded-md border border-gray-200 bg-gray-50 p-3">
          <h4 className="mb-2 text-xs font-medium">{section.title}</h4>
          <div className="space-y-1 text-xs">
            {section.content}
          </div>
        </div>
      ))}
    </div>
  )
} 
