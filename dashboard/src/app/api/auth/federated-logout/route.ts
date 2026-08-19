import { getToken } from 'next-auth/jwt'
import { NextRequest, NextResponse } from 'next/server'

export async function GET(request: NextRequest) {
  const issuer = process.env.KEYCLOAK_ISSUER_URI

  if (!issuer) return NextResponse.json({ url: null })

  const token = await getToken({ req: request })

  if (!token?.idToken) return NextResponse.json({ url: null })

  const logoutUrl = new URL(`${issuer}/protocol/openid-connect/logout`)
  logoutUrl.searchParams.set('id_token_hint', token.idToken)
  logoutUrl.searchParams.set(
    'post_logout_redirect_uri',
    `${process.env.NEXTAUTH_URL ?? request.nextUrl.origin}/api/auth/signin`
  )

  return NextResponse.json({ url: logoutUrl.toString() })
}
