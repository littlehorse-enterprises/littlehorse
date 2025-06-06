import SignInButton from "./signInButton"
import SignOutButton from "./signOutButton"

export default function SignIn() {
    return (
        <div className="flex gap-4">
            <SignInButton />
            <SignOutButton />
        </div>
    )
} 