import { redirect } from "next/navigation"
import { auth } from "@/lib/auth"
import { AuthStatus } from "@/components/auth"

export default async function Home() {
  const session = await auth()

  // Redirect authenticated users to dashboard
  if (session) {
    redirect("/dashboard")
  }

  return (
    <div className="flex flex-col items-center justify-center h-screen p-8">
      <h1 className="mb-8 text-4xl font-bold">LittleHorse Dashboard</h1>
      <div className="mb-8 text-center text-muted-foreground max-w-md">
        Welcome to the LittleHorse Dashboard. Please sign in to continue.
      </div>
      <AuthStatus />
    </div>
  )
}
