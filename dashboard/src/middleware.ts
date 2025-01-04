import { auth } from '@/auth'
import { getToken } from 'next-auth/jwt'
import { NextResponse } from 'next/server'

const withoutAuth = () => {
  NextResponse.next()
}

const withAuth = auth(async req => {
  const token = await getToken({ req, secret: process.env.AUTH_SECRET })
  const baseUrl = req.nextUrl.origin
  const currentPath = req.nextUrl.pathname
  if (!token || token.expiresAt! < Date.now() / 1000) {
    return NextResponse.redirect(`${baseUrl}/api/auth/signin?callbackUrl=${currentPath}`)
  }

  return NextResponse.next()
})

export const config = {
  matcher: ['/((?!api|_next/static|_next/image|images|favicon.ico).*)'],
}
export default process.env.LHD_OAUTH_ENABLED === 'true' ? withAuth : withoutAuth
