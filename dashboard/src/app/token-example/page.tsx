import { getAccessToken, getUser } from "@/lib/session";
import { redirect } from "next/navigation";
import { TokenDisplay } from "@/components/auth";

export default async function TokenExamplePage() {
    const token = await getAccessToken();
    const user = await getUser();

    // Redirect if no token is available
    if (!token) {
        redirect("/");
    }

    // In a real application, you would use the token to make API requests
    // to your backend services or Keycloak-protected resources

    return (
        <div className="container mx-auto py-8">
            <h1 className="text-2xl font-bold mb-4">Server-Side Token Example</h1>

            <div className="bg-white rounded-lg shadow p-6 mb-6">
                <h2 className="text-xl font-semibold mb-4">User Information</h2>
                <p>
                    <span className="font-medium">Name:</span> {user?.name}
                </p>
                <p>
                    <span className="font-medium">Email:</span> {user?.email}
                </p>
            </div>

            <div className="mb-6">
                <h2 className="text-xl font-semibold mb-4">Server-Side Access Token</h2>
                <div className="bg-slate-100 p-4 rounded-md">
                    <pre className="text-xs overflow-auto max-h-32 whitespace-pre-wrap break-all">
                        {token}
                    </pre>
                </div>
            </div>

            <div>
                <h2 className="text-xl font-semibold mb-4">Client-Side Token Display</h2>
                <TokenDisplay />
            </div>
        </div>
    );
} 