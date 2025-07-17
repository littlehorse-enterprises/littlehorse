import 'next-auth'
import 'next-auth/jwt'

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
