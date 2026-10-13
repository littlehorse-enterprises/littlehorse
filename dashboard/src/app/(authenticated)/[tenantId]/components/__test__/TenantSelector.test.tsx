import { fireEvent, render, screen } from '@testing-library/react'
import { TenantSelector } from '../TenantSelector'

const push = jest.fn()
const tenants = ['acme-prod', 'acme-dev', 'legacy', 'four', 'five', 'six', 'seven', 'eight', 'nine']
jest.mock('next/navigation', () => ({
  useParams: () => ({ tenantId: 'acme-prod' }),
  useRouter: () => ({ push }),
}))
jest.mock('@/contexts/WhoAmIContext', () => ({
  useWhoAmI: () => ({ tenants, tenantNames: { 'acme-prod': 'Acme', 'acme-dev': 'Acme' } }),
}))

describe('TenantSelector', () => {
  beforeEach(() => push.mockClear())

  it('shows duplicate names alongside IDs and navigates using the selected ID', () => {
    render(<TenantSelector />)
    fireEvent.click(screen.getByRole('button', { name: 'Acme' }))
    fireEvent.click(screen.getByRole('button', { name: 'Acme acme-dev' }))
    expect(push).toHaveBeenCalledWith('/acme-dev')
  })

  it('searches names and IDs and falls back to the ID for unnamed tenants', () => {
    render(<TenantSelector />)
    fireEvent.click(screen.getByRole('button', { name: 'Acme' }))
    const search = screen.getByRole('textbox', { name: 'Search tenants' })
    fireEvent.change(search, { target: { value: 'ACME' } })
    expect(screen.getByRole('button', { name: 'Acme acme-prod' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Acme acme-dev' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'legacy' })).not.toBeInTheDocument()
    fireEvent.change(search, { target: { value: 'legacy' } })
    expect(screen.getByRole('button', { name: 'legacy' })).toBeInTheDocument()
  })
})
