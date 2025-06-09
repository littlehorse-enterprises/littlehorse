// todo : is there a way to force logout, since this current URL shows a logout button?

import { auth } from "@/auth";

export async function getKeycloakLogoutUrl() {
  const session = await auth();
  if (!session?.user) return null;

  const headers = new Headers();
  headers.set(
    "Authorization",
    "Basic " +
      Buffer.from(
        process.env.KEYCLOAK_CLIENT_ID +
          ":" +
          process.env.KEYCLOAK_CLIENT_SECRET
      ).toString("base64")
  );

  const response = await fetch(
    process.env.KEYCLOAK_ISSUER_URI + "/.well-known/openid-configuration",
    { method: "GET", headers }
  );
  const config = await response.json();

  const endSessionUrl = new URL(config.end_session_endpoint);
  endSessionUrl.searchParams.set("client_id", process.env.KEYCLOAK_CLIENT_ID!);
  endSessionUrl.searchParams.set(
    "post_logout_redirect_uri",
    "http://localhost:3000/auth"
  );

  return endSessionUrl.toString();
}
