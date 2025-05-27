"use client";

import { signOut } from "next-auth/react";
import { Button } from "../ui/button";

interface LogoutButtonProps {
    className?: string;
}

export function LogoutButton({ className }: LogoutButtonProps) {
    return (
        <Button
            onClick={() => signOut({ callbackUrl: "/" })}
            variant="outline"
            className={className}
        >
            Sign Out
        </Button>
    );
} 