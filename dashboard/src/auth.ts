import type { Session } from 'next-auth'
import NextAuth from 'next-auth'
import 'next-auth/jwt'
import type { JWT } from 'next-auth/jwt'
import Keycloak from 'next-auth/providers/keycloak'

declare module 'next-auth' {
  interface Session {
    accessToken: string
    expiresAt: number
    idToken: string
  }
}

declare module 'next-auth/jwt' {
  interface JWT {
    accessToken: string
    expiresAt: number
    idToken: string
  }
}

const OAUTH_ENABLED = process.env.AUTH_KEYCLOAK_ISSUER && process.env.AUTH_KEYCLOAK_ID && process.env.AUTH_KEYCLOAK_SECRET

export const { handlers, signIn, signOut, auth } = NextAuth({
  pages: {
    signIn: '/api/signin',
  },
  secret: process.env.AUTH_SECRET || (OAUTH_ENABLED ? undefined : 'fallback-secret-for-disabled-auth'),
  providers: OAUTH_ENABLED
    ? [Keycloak]
    : [],
  callbacks: {
    async jwt({ token, account }) {
      if (account?.provider === 'keycloak') {
        token.accessToken = account.access_token || ''
        token.idToken = account.id_token || ''
        token.expiresAt = account.expires_at || 0
      }
      return token
    },
    async session({ session, token }: { session: Session; token: JWT }) {
      session.accessToken = token.accessToken
      session.idToken = token.idToken
      session.expiresAt = token.expiresAt
      return session
    },
    async authorized({ auth }) {
      if (!OAUTH_ENABLED) return true

      const token = auth?.accessToken
      if (!token) return false
      if (auth?.expiresAt && auth.expiresAt < Date.now() / 1000) return false

      try {
        const { ok } = await fetch(`${process.env.AUTH_KEYCLOAK_ISSUER}/protocol/openid-connect/userinfo`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        })

        return ok
      } catch {
        return false
      }
    },
  },
  events: {
    async signOut(message) {
      if ('token' in message && message.token && message.token.idToken) {
        const url = `${process.env.AUTH_KEYCLOAK_ISSUER}/protocol/openid-connect/logout?id_token_hint=${message.token.idToken}`;
        await fetch(url, {
          method: "GET",
          headers: { Accept: "application/json" },
        });
      }
    },
  },
})