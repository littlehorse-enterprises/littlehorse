"use client"
import { Button } from "@/components/ui/button"
import { handleSignIn } from "@/actions/handleSignIn"

export default function SignInButton() {
    return (
        <Button
            onClick={handleSignIn}
            className="bg-blue-500 text-white hover:bg-blue-600"
        >
            Sign In
        </Button>
    )
} 