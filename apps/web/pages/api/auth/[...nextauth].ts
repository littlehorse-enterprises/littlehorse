import NextAuth from 'next-auth'
import GithubProvider from 'next-auth/providers/github'
import GoogleProvider from 'next-auth/providers/google'
import AzureADProvider from 'next-auth/providers/azure-ad'
import OktaProvider from 'next-auth/providers/okta'
import KeycloakProvider from 'next-auth/providers/keycloak'


const providers:any = []
if(process.env.GITHUB_ID && process.env.GITHUB_ID != '') {
  providers.push(
    GithubProvider({
      clientId: process.env.GITHUB_ID ,
      clientSecret: process.env.GITHUB_SECRET!,
    })
  )
}
if(process.env.GOOGLE_ID && process.env.GOOGLE_ID != '') {
  providers.push(
    GoogleProvider({
      clientId: process.env.GOOGLE_ID ,
      clientSecret: process.env.GOOGLE_SECRET!,
    }),
  )
}
if(process.env.AZURE_AD_CLIENT_ID && process.env.AZURE_AD_CLIENT_ID != '') {
  providers.push(
    AzureADProvider({
      clientId: process.env.AZURE_AD_CLIENT_ID ,
      clientSecret: process.env.AZURE_AD_CLIENT_SECRET!,
      tenantId: process.env.AZURE_AD_TENANT_ID
    }),
  )
}
if(process.env.OKTA_CLIENT_ID && process.env.OKTA_CLIENT_ID != '') {
  providers.push(
    OktaProvider({
      clientId: process.env.OKTA_CLIENT_ID ,
      clientSecret: process.env.OKTA_CLIENT_SECRET!,
      issuer: process.env.OKTA_ISSUER_URI
    })
  )
}
if(process.env.KEYCLOAK_CLIENT_ID && process.env.KEYCLOAK_CLIENT_ID != '') {
  providers.push(
    KeycloakProvider({
      clientId: process.env.KEYCLOAK_CLIENT_ID ,
      clientSecret: process.env.KEYCLOAK_CLIENT_SECRET!,
      issuer: process.env.KEYCLOAK_ISSUER_URI
    })
  )
}




export const authOptions = {
  pages:{
    signIn:'/signin'
  },
  secret: process.env.AUTH_SECRET,
  providers,

  callbacks: {
    async jwt({ token, account }) {
      // Persist the OAuth access_token to the token right after signin
      if (account) {
        token.accessToken = account.access_token
      }
      return token
    },
    async session({ session, token, user }) {
      // Send properties to the client, like an access_token from a provider.
      session.accessToken = token.accessToken
      return session
    }
  }
}
export default NextAuth(authOptions)