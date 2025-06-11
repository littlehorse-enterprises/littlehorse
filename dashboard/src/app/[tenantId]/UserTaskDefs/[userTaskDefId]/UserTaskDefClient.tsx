"use client";

import { Badge } from "@littlehorse-enterprises/ui-library/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@littlehorse-enterprises/ui-library/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@littlehorse-enterprises/ui-library/table";
import { UserTaskDef } from "littlehorse-client/proto";
import { ArrowLeft, Clock, Hash } from "lucide-react";
import Link from "next/link";
import { useParams } from "next/navigation";

interface UserTaskDefClientProps {
  userTaskDef: UserTaskDef;
}

export default function UserTaskDefClient({ userTaskDef }: UserTaskDefClientProps) {
  const { tenantId, userTaskDefId } = useParams<{ tenantId: string; userTaskDefId: string }>();

  return (
    <div className="container mx-auto py-6">
      {/* Header */}
      <div className="mb-6 flex items-center gap-4">
        <Link href={`/${tenantId}`} className="flex items-center gap-2 text-muted-foreground hover:text-foreground">
          <ArrowLeft className="h-4 w-4" />
          Go back to UserTaskDefs
        </Link>
      </div>

      <div className="mb-8">
        <h1 className="text-4xl font-bold flex items-center gap-3">
          <Hash className="h-8 w-8" />
          {userTaskDef.name}
        </h1>
        <div className="flex items-center gap-2 mt-2">
          <Badge variant="outline" className="font-mono">v{userTaskDef.version}</Badge>
        </div>
        <p className="text-muted-foreground mt-2">UserTaskDef</p>
      </div>

      <div className="grid gap-6">
        {/* Fields */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">Fields</CardTitle>
          </CardHeader>
          <CardContent>
            {userTaskDef.fields && userTaskDef.fields.length > 0 ? (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Display</TableHead>
                    <TableHead>Type</TableHead>
                    <TableHead>Required</TableHead>
                    <TableHead>Description</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {userTaskDef.fields.map((field, idx) => (
                    <TableRow key={idx}>
                      <TableCell className="font-mono text-purple-600">{field.name}</TableCell>
                      <TableCell className="font-mono text-muted-foreground">{field.displayName || "-"}</TableCell>
                      <TableCell>
                        <Badge variant="outline" className="font-mono">{field.type}</Badge>
                      </TableCell>
                      <TableCell>
                        {field.required ? <Badge variant="destructive">Required</Badge> : <Badge variant="outline">Optional</Badge>}
                      </TableCell>
                      <TableCell className="text-xs text-muted-foreground">{field.description || ""}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            ) : (
              <div className="text-center py-8 text-muted-foreground">No fields defined for this user task</div>
            )}
          </CardContent>
        </Card>

        {/* Basic Information */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5" />
              Basic Information
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Name</p>
                <p className="text-lg font-mono">{userTaskDef.name}</p>
              </div>
              <div>
                <p className="text-sm font-medium text-muted-foreground">Version</p>
                <p className="text-lg font-mono">{userTaskDef.version}</p>
              </div>
              <div>
                <p className="text-sm font-medium text-muted-foreground">Created At</p>
                <p className="text-lg">{userTaskDef.createdAt ? new Date(userTaskDef.createdAt).toLocaleString() : "N/A"}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Related User Task Runs (placeholder) */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">Related User Task Runs</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-center py-8 text-muted-foreground">(User Task Runs table coming soon)</div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
} 
