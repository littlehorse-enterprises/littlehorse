import { JWT } from 'next-auth/jwt'
import NextAuth, { DefaultSession } from 'next-auth'

declare module 'next-auth/jwt' {
  interface JWT {
    accessToken?: string
    expiresAt?: number
  }
}

declare module 'next-auth' {
  interface Session {
    accessToken: string
  }
}
