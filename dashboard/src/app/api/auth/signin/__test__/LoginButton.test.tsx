import { fireEvent, render } from '@testing-library/react'
import { signIn } from 'next-auth/react'
import React from 'react'
import { LoginButton } from '../LoginButton'

jest.mock('next-auth/react', () => ({ signIn: jest.fn() }))
jest.mock('next/navigation', () => ({ useSearchParams: () => new URLSearchParams('callbackUrl=/tenant') }))

describe('LoginButton', () => {
  it('asks the provider to re-authenticate so a different user can sign in', () => {
    const { getByText } = render(<LoginButton id="keycloak" name="Keycloak" />)

    fireEvent.click(getByText('Login with Keycloak'))

    expect(signIn).toHaveBeenCalledWith('keycloak', { callbackUrl: '/tenant' }, { prompt: 'login' })
  })
})
