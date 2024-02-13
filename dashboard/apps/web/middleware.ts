import type { NextRequest } from 'next/server'
import { NextResponse } from 'next/server'
import type { TokenWithJWTInfo } from './pages/api/auth/[...nextauth]'
import { getToken } from 'next-auth/jwt'
import type { JWT } from 'next-auth/jwt'

export async function middleware(req: NextRequest) {
    if (process.env.LHD_OAUTH_ENABLED === 'true') {

        const token: JWT | null = await getToken({ req, secret: process.env.AUTH_SECRET })
        const { pathname } = req.nextUrl
        const origin = req.nextUrl.origin

        const isPublicRoute =
        pathname.includes('/api/auth') ||
        pathname.includes('/signin') ||
        pathname.includes('.png') ||
        pathname.includes('.svg') ||
        pathname.includes('/favicon.ico') ||
        pathname.includes('jpg') ||
        pathname.includes('_next') ||
        pathname.includes('/errors')

        if (isPublicRoute) {
            return NextResponse.next()
        }

        const tokenHasExpired = token && new Date() > new Date((token as unknown as TokenWithJWTInfo).expireTime * 1000)
        if (tokenHasExpired && !pathname.includes('/signin')) {
            const response = NextResponse.redirect(new URL('/signin', origin))
            return response
        }
    }

    return NextResponse.next()
}
