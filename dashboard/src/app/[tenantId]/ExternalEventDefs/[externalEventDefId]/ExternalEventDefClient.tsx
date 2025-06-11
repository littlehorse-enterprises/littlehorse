"use client"

import { useExecuteRPCWithSWR } from "@/hooks/useExecuteRPCWithSWR";
import { SEARCH_LIMIT_DEFAULT, SEARCH_LIMITS } from "@/utils/ui/constants";
import { Card, CardContent, CardHeader, CardTitle } from "@littlehorse-enterprises/ui-library/card";
import { Label } from "@littlehorse-enterprises/ui-library/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@littlehorse-enterprises/ui-library/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@littlehorse-enterprises/ui-library/table";
import { ExternalEventDef } from "littlehorse-client/proto";
import { Activity, ArrowLeft, Clock, Hash, Loader2 } from "lucide-react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useState } from "react";

interface ExternalEventDefClientProps {
  externalEventDef: ExternalEventDef;
}

export default function ExternalEventDefClient({ externalEventDef }: ExternalEventDefClientProps) {
  const [limit, setLimit] = useState(SEARCH_LIMIT_DEFAULT);
  const { tenantId, externalEventDefId } = useParams<{ tenantId: string; externalEventDefId: string }>();

  // Fetch related ExternalEvents
  const { data: relatedEvents } = useExecuteRPCWithSWR("searchExternalEvent", {
    externalEventDefId: { name: externalEventDefId },
    limit,
  });
  return (
    <div className="container mx-auto py-6">
      {/* Header */}
      <div className="mb-6 flex items-center gap-4">
        <Link href={`/${tenantId}`} className="flex items-center gap-2 text-muted-foreground hover:text-foreground">
          <ArrowLeft className="h-4 w-4" />
          Back to Metadata
        </Link>
      </div>

      <div className="mb-8">
        <h1 className="text-4xl font-bold flex items-center gap-3">
          <Hash className="h-8 w-8" />
          {externalEventDef?.id?.name || externalEventDefId}
        </h1>
        <p className="text-muted-foreground mt-2">
          External Event Definition
        </p>
      </div>

      <div className="grid gap-6">
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
                <p className="text-lg font-mono">{externalEventDef.id?.name || externalEventDefId}</p>
              </div>
              <div>
                <p className="text-sm font-medium text-muted-foreground">Created At</p>
                <p className="text-lg">{externalEventDef.createdAt ? new Date(externalEventDef.createdAt).toLocaleString() : "N/A"}</p>
              </div>
              <div>
                <p className="text-sm font-medium text-muted-foreground">Retention Policy</p>
                <p className="text-lg font-mono">
                  {externalEventDef.retentionPolicy
                    ? JSON.stringify(externalEventDef.retentionPolicy, null, 2)
                    : "N/A"}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Related External Events */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Activity className="h-5 w-5" />
              Related External Events
            </CardTitle>
          </CardHeader>
          <CardContent>
              <>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>WfRun Id</TableHead>
                      <TableHead>GUID</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                  {!relatedEvents ?(
                      <TableRow>
                        <TableCell colSpan={2} className="text-center py-8 text-muted-foreground">
                          <Loader2 className="inline animate-spin" />
                        </TableCell>
                      </TableRow>
                    ): relatedEvents.results.length === 0 ?(<TableRow>
                        <TableCell colSpan={2} className="text-center py-8 text-muted-foreground">
                          No ExternalEvents found for this ExternalEventDef
                        </TableCell>
                      </TableRow>) :(
                      relatedEvents.results.map((externalEventId, index) => (
                        <TableRow key={index}>
                          <TableCell>
                            {externalEventId.wfRunId && externalEventId.wfRunId.id}
                          </TableCell>
                          <TableCell>
                            {externalEventId.guid}
                          </TableCell>
                        </TableRow>
                      ))
                    )}

                  </TableBody>
                </Table>
                {/* Limit dropdown */}
                <div className="mt-4 flex items-center justify-end">
                  <Label className="mr-2 text-sm">Limit:</Label>
                  <Select
                    value={limit.toString()}
                    onValueChange={value => setLimit(Number(value) as typeof SEARCH_LIMITS[number])}
                  >
                    <SelectTrigger className="w-fit">
                      <SelectValue placeholder="Items per load" />
                    </SelectTrigger>
                    <SelectContent>
                      {SEARCH_LIMITS.map(limit => (
                        <SelectItem key={limit} value={limit.toString()}>
                          {limit}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </>
          </CardContent>
        </Card>
      </div>
    </div>
  );
} 
