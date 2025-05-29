import NextAuth from "next-auth";
import Keycloak from "next-auth/providers/keycloak";
import "next-auth/jwt";

declare module "next-auth" {
  interface Session {
    accessToken?: string;
  }
}

declare module "next-auth/jwt" {
  interface JWT {
    accessToken?: string;
  }
}

export const { handlers, signIn, signOut, auth } = NextAuth({
  pages: {
    signIn: "/api/signin",
  },
  providers: [
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
      }
      return token;
    },
    async session({ session, token }) {
      session.accessToken = token.accessToken;
      return session;
    },
    async authorized({ auth }) {
      return !!auth?.user;
    },
  },
});
