import { getServerSession } from 'next-auth'
import nextAuth from 'next-auth/middleware'
import { NextResponse } from 'next/server'
import { authOptions } from './app/api/auth/[...nextauth]/authOptions'
import { getToken } from 'next-auth/jwt'

const withoutAuth = () => {
  NextResponse.next()
}

const withAuth = nextAuth(async req => {
  const token = await getToken({ req })
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
