import type { Session } from 'next-auth'
import NextAuth from 'next-auth'
import type { JWT } from 'next-auth/jwt'
import KeycloakProvider from 'next-auth/providers/keycloak'


const providers:any = []
if (process.env.KEYCLOAK_CLIENT_ID &&
    process.env.KEYCLOAK_CLIENT_ID !== '' &&
    process.env.KEYCLOAK_CLIENT_SECRET &&
    process.env.KEYCLOAK_CLIENT_SECRET !== '') {
    providers.push(
        KeycloakProvider({
            clientId: process.env.KEYCLOAK_CLIENT_ID ,
            clientSecret: process.env.KEYCLOAK_CLIENT_SECRET,
            issuer: process.env.KEYCLOAK_ISSUER_URI
        })
    )
}

async function logoutFromKeyCloack(jwt: JWT) {
    const { id_token } = jwt
    
    try {
        const params = new URLSearchParams()
        params.append('id_token_hint', id_token as string)
        await fetch(`${process.env.KEYCLOAK_ISSUER_URI}/protocol/openid-connect/logout?${params.toString()}`)
    } catch (e: any) {
        console.error('Unable to perform post-logout handshake', e)
    }
}



export const authOptions = {
    pages:{
        signIn:'/signin'
    },
    secret: process.env.AUTH_SECRET,
    providers,
    callbacks: {
        jwt({ token, account }) {
            if (__AUTHENTICATION_ENABLED__) {
                if (account) {
                    token.accessToken = account.access_token
                    token.exp = account.expires_at
                    token.expireTime = account.expires_at
                    token.id_token = account.id_token
                }
                
                const tokenHasExpired = new Date() > new Date(token.expireTime * 1000)

                if (tokenHasExpired) {
                    logoutFromKeyCloack(token)
                }
            }

            return token as TokenWithJWTInfo
        },
        session({ session, token }) {
            if (__AUTHENTICATION_ENABLED__) {
                session.accessToken = token.accessToken
                session.expireTime = token.expireTime
                session.id_token = token.id_token
            }

            return session as SessionWithJWTExpireTime
        },
    },
    events: {
        signOut: ({ token }) => {
            logoutFromKeyCloack(token)
        }
    }
}

export interface SessionWithJWTExpireTime extends Session {
    expireTime: number,
    accessToken: string,
    id_token: string
}

export interface TokenWithJWTInfo extends JWT {
    expireTime: number,
    accessToken: string,
    id_token: string
}

export default NextAuth(authOptions)