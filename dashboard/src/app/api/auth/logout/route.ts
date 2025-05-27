import { auth } from "@/lib/auth";
import { NextResponse } from "next/server";

export async function GET() {
  const session = await auth();

  // Return early if no session exists
  if (!session) {
    return new NextResponse(null, { status: 401 });
  }

  // For full federated logout, you would need to get the id_token
  // from the session and call the Keycloak logout endpoint

  return NextResponse.json({ success: true });
}
