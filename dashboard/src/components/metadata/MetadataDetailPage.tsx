import { ReactNode } from "react"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { MetadataHeader } from "./MetadataHeader"

export interface TabItem {
    value: string
    label: string
    content: ReactNode
}

interface MetadataDetailPageProps {
    title: string
    type: string
    version?: string | string[]
    backUrl: string
    backText?: string
    activeTab: string
    onTabChange: (value: string) => void
    tabs: TabItem[]
}

export function MetadataDetailPage({
    title,
    type,
    version,
    backUrl,
    backText,
    activeTab,
    onTabChange,
    tabs
}: MetadataDetailPageProps) {
    return (
        <div className="container mx-auto py-6">
            <MetadataHeader
                title={title}
                type={type}
                version={version}
                backUrl={backUrl}
                backText={backText}
            />

            <Tabs value={activeTab} onValueChange={onTabChange} className="w-full">
                <TabsList className="mb-8 w-full justify-start rounded-lg">
                    {tabs.map((tab) => (
                        <TabsTrigger key={tab.value} value={tab.value}>
                            {tab.label}
                        </TabsTrigger>
                    ))}
                </TabsList>

                {tabs.map((tab) => (
                    <TabsContent key={tab.value} value={tab.value}>
                        {tab.content}
                    </TabsContent>
                ))}
            </Tabs>
        </div>
    )
} 