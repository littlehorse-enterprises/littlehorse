"use client"
import { handleSignOut } from "@/actions/handleSignOut"
import { Button } from "@/components/ui/button"

export default function SignOutButton() {
    return (
        <Button
            onClick={handleSignOut}
            className="bg-red-500 text-white hover:bg-red-600"
        >
            Sign Out
        </Button>
    )
} 