import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import Link from "next/link"

interface WorkflowSpec {
    id: string
    name: string
    version: string
}

interface WorkflowSpecUsageProps {
    title?: string
    workflowSpecs: WorkflowSpec[]
}

export function WorkflowSpecUsage({
    title = "Workflow Specification Usage",
    workflowSpecs
}: WorkflowSpecUsageProps) {
    return (
        <Card className="border-0">
            <CardHeader>
                <CardTitle>{title}</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="space-y-4">
                    {workflowSpecs.length === 0 ? (
                        <p className="text-muted-foreground">No workflow specifications found.</p>
                    ) : (
                        <ul className="space-y-3">
                            {workflowSpecs.map((wfSpec) => (
                                <li key={wfSpec.id} className="flex items-center justify-between p-3 bg-muted rounded-lg hover:bg-muted/70">
                                    <div>
                                        <p className="font-medium">{wfSpec.name}</p>
                                        <div className="flex items-center gap-2 mt-1">
                                            <Badge variant="outline" className="font-mono">
                                                {wfSpec.version}
                                            </Badge>
                                        </div>
                                    </div>
                                    <Link href={`/diagram?id=${wfSpec.id}`}>
                                        <Button variant="ghost" size="sm" className="gap-1">
                                            View Diagram
                                        </Button>
                                    </Link>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </CardContent>
        </Card>
    )
} 