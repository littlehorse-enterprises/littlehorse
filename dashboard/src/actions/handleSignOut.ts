"use server";

import { signOut } from "@/auth";
import { getKeycloakLogoutUrl } from "@/utils/auth/getKeycloakLogoutUrl";
import { redirect } from "next/navigation";

export async function handleSignOut() {
  await signOut({ redirect: false });
  const logoutUrl = await getKeycloakLogoutUrl();

  if (logoutUrl) {
    redirect(logoutUrl);
  }
}
