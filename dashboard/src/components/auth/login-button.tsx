"use client";

import { signIn } from "next-auth/react";
import { Button } from "../ui/button";

interface LoginButtonProps {
    callbackUrl?: string;
    className?: string;
}

export function LoginButton({
    callbackUrl = "/dashboard",
    className
}: LoginButtonProps) {
    return (
        <Button
            onClick={() => signIn("keycloak", { callbackUrl })}
            className={className}
        >
            Sign In
        </Button>
    );
} 