import { isTokenExpired, validateAccessToken } from '@/utils/auth/authUtil'
import NextAuth from 'next-auth'
import 'next-auth/jwt'
import Keycloak from 'next-auth/providers/keycloak'

declare module 'next-auth' {
  interface Session {
    accessToken?: string
    expiresAt?: number
  }
}

declare module 'next-auth/jwt' {
  interface JWT {
    accessToken?: string
    expiresAt?: number
  }
}

const OAUTH_ENABLED =
  process.env.KEYCLOAK_ISSUER_URI && process.env.KEYCLOAK_CLIENT_ID && process.env.KEYCLOAK_CLIENT_SECRET

export const { handlers, signIn, signOut, auth } = NextAuth({
  pages: {
    signIn: '/api/signin',
  },
  providers: OAUTH_ENABLED
    ? [
        Keycloak({
          issuer: process.env.KEYCLOAK_ISSUER_URI,
          clientId: process.env.KEYCLOAK_CLIENT_ID,
          clientSecret: process.env.KEYCLOAK_CLIENT_SECRET,
        }),
      ]
    : [],
  callbacks: {
    jwt({ token, account }) {
      if (account?.provider === 'keycloak') {
        token.accessToken = account.access_token
        token.expiresAt = account.expires_at
      }
      return token
    },
    async session({ session, token }) {
      session.accessToken = token.accessToken
      session.expiresAt = token.expiresAt
      return session
    },
    async authorized({ auth }) {
      if (!OAUTH_ENABLED) return true

      const token = auth?.accessToken

      return !!(token && (await validateAccessToken(token)) && !isTokenExpired(auth?.expiresAt))
    },
  },
})
