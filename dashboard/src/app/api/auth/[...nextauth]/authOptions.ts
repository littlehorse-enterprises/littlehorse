import { AuthOptions } from 'next-auth'
import { Provider } from 'next-auth/providers/index'
import KeycloakProvider from 'next-auth/providers/keycloak'

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
          idToken: account.id_token,
        }
      }
      return token
    },

    session: async ({ token, session }) => {
      return {
        ...session,
        accessToken: token.accessToken,
        idToken: token.idToken,
      }
    },
  },
  events: {
    signOut: async ({ token }: any) => {
      const url = `${process.env.KEYCLOAK_ISSUER_URI}/protocol/openid-connect/logout?id_token_hint=${token.idToken}`
      await fetch(url, { method: 'GET', headers: { Accept: 'application/json' } })
    },
  },
}
