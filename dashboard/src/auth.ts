import NextAuth from "next-auth";
import Keycloak from "next-auth/providers/keycloak";
import "next-auth/jwt";
import { validateAccessToken, isTokenExpired } from "@/utils/authUtil";

declare module "next-auth" {
  interface Session {
    accessToken?: string;
    expiresAt?: number;
  }
}

declare module "next-auth/jwt" {
  interface JWT {
    accessToken?: string;
    expiresAt?: number;
  }
}

export const { handlers, signIn, signOut, auth } = NextAuth({
  pages: {
    signIn: "/api/signin",
  },
  providers: process.env.LHD_OAUTH_ENABLED === "false" || !process.env.LHD_OAUTH_ENABLED ? [] : [
    Keycloak({
      issuer: process.env.KEYCLOAK_ISSUER_URI,
      clientId: process.env.KEYCLOAK_CLIENT_ID,
      clientSecret: process.env.KEYCLOAK_CLIENT_SECRET,
    }),
  ],
  callbacks: {
    jwt({ token, account }) {
      if (account?.provider === "keycloak") {
        token.accessToken = account.access_token;
        token.expiresAt = account.expires_at;
      }
      return token;
    },
    async session({ session, token }) {
      session.accessToken = token.accessToken;
      session.expiresAt = token.expiresAt;
      return session;
    },
    async authorized({ auth }) {
      if (process.env.LHD_OAUTH_ENABLED === "false" || !process.env.LHD_OAUTH_ENABLED) 
        return true;
      
      const token = auth?.accessToken;

      return !!(
        token &&
        (await validateAccessToken(token)) &&
        !isTokenExpired(auth?.expiresAt)
      );
    },
  },
});