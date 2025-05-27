"use client";

import { useSession } from "next-auth/react";
import { LoginButton } from "./login-button";
import { LogoutButton } from "./logout-button";

export function AuthStatus() {
    const { data: session, status } = useSession();
    const isLoading = status === "loading";
    console.log("session data:", session);

    if (isLoading) {
        return <div>Loading...</div>;
    }

    if (!session) {
        return <LoginButton />;
    }

    return (
        <div className="flex flex-col gap-4">
            <div className="flex items-center gap-4">
                <p className="text-sm">
                    Signed in as <span className="font-medium">{session.user?.name || session.user?.email}</span>
                </p>
                <LogoutButton />
            </div>
            {session.accessToken && (
                <div className="mt-2">
                    <p className="text-sm mb-1">Access Token:</p>
                    <pre className="bg-gray-100 p-2 rounded text-xs overflow-auto max-w-xl max-h-32">
                        {session.accessToken}
                    </pre>
                </div>
            )}
        </div>
    );
} 