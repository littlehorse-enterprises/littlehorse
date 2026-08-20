import 'next-auth'
import 'next-auth/jwt'

declare module 'next-auth/jwt' {
  interface JWT {
    accessToken?: string
    expiresAt?: number
    idToken?: string
  }
}

declare module 'next-auth' {
  interface Session {
    accessToken: string
    idToken?: string
  }
}
