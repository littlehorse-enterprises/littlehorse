import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

interface OverviewCardProps {
    title: string
    value: string
    /**
     * Whether to display the value as monospace font
     * @default true
     */
    mono?: boolean
}

export function OverviewCard({ title, value, mono = true }: OverviewCardProps) {
    return (
        <Card>
            <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium">{title}</CardTitle>
            </CardHeader>
            <CardContent>
                <p className={`text-lg ${mono ? "font-mono" : ""}`}>{value}</p>
            </CardContent>
        </Card>
    )
} 