"use client"
import { handleSignIn } from "@/actions/handleSignIn"
import { Button } from "@littlehorse-enterprises/ui-library/button"

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
