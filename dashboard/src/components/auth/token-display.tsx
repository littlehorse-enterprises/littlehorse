"use client";

import { useSession } from "next-auth/react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Copy } from "lucide-react";
import { useState } from "react";

export function TokenDisplay() {
    const { data: session } = useSession();
    const [copied, setCopied] = useState(false);

    // No session or no access token
    if (!session?.accessToken) {
        return (
            <Card>
                <CardHeader>
                    <CardTitle>Access Token</CardTitle>
                </CardHeader>
                <CardContent>
                    <p className="text-muted-foreground">No access token available</p>
                </CardContent>
            </Card>
        );
    }

    const copyToClipboard = () => {
        navigator.clipboard.writeText(session.accessToken as string);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    return (
        <Card>
            <CardHeader className="flex flex-row items-center justify-between">
                <CardTitle>Access Token</CardTitle>
                <Button
                    size="sm"
                    variant="outline"
                    onClick={copyToClipboard}
                    className="flex items-center gap-1"
                >
                    <Copy className="h-4 w-4" />
                    {copied ? "Copied!" : "Copy"}
                </Button>
            </CardHeader>
            <CardContent>
                <div className="bg-slate-100 p-4 rounded-md">
                    <pre className="text-xs overflow-auto max-h-64 whitespace-pre-wrap break-all">
                        {session.accessToken}
                    </pre>
                </div>
                <p className="mt-4 text-sm text-muted-foreground">
                    This token can be used to make authenticated requests to your backend services.
                </p>
            </CardContent>
        </Card>
    );
} 