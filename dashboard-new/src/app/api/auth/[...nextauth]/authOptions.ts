import { AuthOptions } from 'next-auth'
import { Provider } from 'next-auth/providers/index'
import KeycloakProvider from 'next-auth/providers/keycloak'
import { signOut } from 'next-auth/react'

const providers: Provider[] = []

if (
  process.env.KEYCLOAK_CLIENT_ID &&
  process.env.KEYCLOAK_CLIENT_ID !== '' &&
  process.env.KEYCLOAK_CLIENT_SECRET &&
  process.env.KEYCLOAK_CLIENT_SECRET !== ''
) {
  providers.push(
    KeycloakProvider({
      clientId: process.env.KEYCLOAK_CLIENT_ID,
      clientSecret: process.env.KEYCLOAK_CLIENT_SECRET,
      issuer: process.env.KEYCLOAK_ISSUER_URI,
    })
  )
}

export const authOptions: AuthOptions = {
  providers,
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
}
