"use client"
import { handleSignOut } from "@/actions/handleSignOut"
import { Button } from "@littlehorse-enterprises/ui-library/button"

export default function SignOutButton() {
    return (
        <Button
            onClick={handleSignOut}
            variant="destructive"
        >
            Sign Out
        </Button>
    )
}
