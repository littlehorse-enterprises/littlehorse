export async function validateAccessToken(token: string | undefined): Promise<boolean> {
  if (!token) {
    return false
  }

  try {
    const response = await fetch(`${process.env.KEYCLOAK_ISSUER_URI}/protocol/openid-connect/userinfo`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

    return response.ok
  } catch {
    return false
  }
}

export function isTokenExpired(expiresAt: number | undefined): boolean {
  if (!expiresAt) {
    return false
  }

  return expiresAt < Date.now() / 1000
}
