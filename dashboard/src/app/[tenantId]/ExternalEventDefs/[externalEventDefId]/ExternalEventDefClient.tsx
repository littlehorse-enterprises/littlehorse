"use client"

import { useExecuteRPCWithSWR } from "@/hooks/useExecuteRPCWithSWR";
import { Card, CardContent, CardHeader, CardTitle } from "@littlehorse-enterprises/ui-library/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@littlehorse-enterprises/ui-library/table";
import { Activity, ArrowLeft, Clock, Hash } from "lucide-react";
import Link from "next/link";
import { useState } from "react";

interface ExternalEventDefClientProps {
  externalEventDefId: string;
  tenantId: string;
}

export default function ExternalEventDefClient({ externalEventDefId, tenantId }: ExternalEventDefClientProps) {
  const [itemsPerLoad, setItemsPerLoad] = useState(10);

  // Fetch ExternalEventDef details
  const { data: externalEventDef } = useExecuteRPCWithSWR("getExternalEventDef", {
    name: externalEventDefId,
  });

  // Fetch related ExternalEvents
  const { data: relatedEvents } = useExecuteRPCWithSWR("searchExternalEvent", {
    externalEventDefId: { name: externalEventDefId },
    limit: itemsPerLoad,
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
                <p className="text-lg font-mono">{externalEventDef?.id?.name || externalEventDefId}</p>
              </div>
              <div>
                <p className="text-sm font-medium text-muted-foreground">Created At</p>
                <p className="text-lg">{externalEventDef?.createdAt ? new Date(externalEventDef.createdAt).toLocaleString() : "N/A"}</p>
              </div>
              <div>
                <p className="text-sm font-medium text-muted-foreground">Retention Policy</p>
                <p className="text-lg font-mono">
                  {externalEventDef?.retentionPolicy
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
            {!relatedEvents ? (
              <div className="text-center py-8 text-muted-foreground">
                Loading events...
              </div>
            ) : (
              <>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>ID</TableHead>
                      <TableHead>WfRun Id</TableHead>
                      <TableHead>GUID</TableHead>
                      <TableHead>Created At</TableHead>
                      <TableHead>Content</TableHead>
                      <TableHead>Claimed</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {relatedEvents.results.length > 0 ? (
                      <></>
                    ) : (
                      <TableRow>
                        <TableCell colSpan={6} className="text-center py-8 text-muted-foreground">
                          No related events found for this ExternalEventDef
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
                {/* Items per load dropdown */}
                <div className="mt-4 flex justify-end">
                  <label className="mr-2 text-sm">Items per load:</label>
                  <select
                    className="border rounded px-2 py-1"
                    value={itemsPerLoad}
                    onChange={e => setItemsPerLoad(Number(e.target.value))}
                  >
                    <option value={10}>10</option>
                    <option value={20}>20</option>
                    <option value={50}>50</option>
                  </select>
                </div>
              </>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
} 
