'use server'

import { signOut } from '@/auth'
import { redirect } from 'next/navigation'

export async function handleSignOut() {
  await signOut({ redirect: false })
  redirect('/auth')
}
