import NextAuth from 'next-auth'
import Keycloak from 'next-auth/providers/keycloak'

export const { auth, handlers, signIn, signOut } = NextAuth({
  providers: [Keycloak],
  callbacks: {
    jwt: async ({ token, account }) => {
      if (account) {
        return {
          ...token,
          accessToken: account.access_token,
          expiresAt: account.expires_at,
        }
      }
      return token
    },

    session: async ({ token, session }) => {
      return {
        ...session,
        accessToken: token.accessToken,
      }
    },
  },
})
