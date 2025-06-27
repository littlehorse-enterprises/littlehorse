import { ReactNode, Children } from 'react'
import { Separator } from '@littlehorse-enterprises/ui-library/separator'
import { SectionProvider, useSectionContext } from '@/context/section-context'

interface SectionProps {
    title: string
    children?: ReactNode
}

export function Section({ title, children }: SectionProps) {
    const { isNested } = useSectionContext()

    return (
        <SectionProvider isNested={true}>
            {isNested && <Separator className="mb-2 mt-2" />}
            <div className="rounded-md border border-gray-200 bg-gray-50 p-2 w-full">
                <h4 className="mb-2 text-xs font-medium">{title}</h4>
                <div className="space-y-1 text-xs">
                    {Children.count(children) === 0 && (
                        <div className="text-gray-500">No data</div>
                    )}
                    {children}
                </div>
            </div>
        </SectionProvider>
    )
} 