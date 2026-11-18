import { render, screen } from '@testing-library/react'
import { StructDef, VariableType } from 'littlehorse-client/proto'
import { ResultStructDef } from '../ResultStructDef'

jest.mock('@/app/(authenticated)/[tenantId]/components/LinkWithTenant', () => ({
  __esModule: true,
  default: ({ href, children, ...props }: React.ComponentProps<'a'>) => (
    <a href={href} {...props}>
      {children}
    </a>
  ),
}))

describe('ResultStructDef', () => {
  const structDefId = { name: 'item-request-form', version: 2 }

  it('links to the StructDef and lists its fields', () => {
    const structDef: StructDef = {
      id: structDefId,
      structDef: {
        fields: {
          requestedItem: {
            fieldType: {
              definedType: { oneofKind: 'primitiveType', primitiveType: VariableType.STR },
              masked: false,
            },
            isNullable: false,
          },
          ssn: {
            fieldType: {
              definedType: { oneofKind: 'primitiveType', primitiveType: VariableType.STR },
              masked: true,
            },
            isNullable: true,
          },
        },
      },
    }

    render(<ResultStructDef structDefId={structDefId} structDef={structDef} />)

    expect(screen.getByRole('link', { name: 'Struct<item-request-form,2>' })).toHaveAttribute(
      'href',
      '/structDef/item-request-form/2'
    )
    expect(screen.getByText('requestedItem')).toBeInTheDocument()
    expect(screen.getByText('ssn')).toBeInTheDocument()
    expect(screen.getByText('Required')).toBeInTheDocument()
    expect(screen.getByText('Masked')).toBeInTheDocument()
  })

  it('still links to the StructDef when it could not be loaded', () => {
    render(<ResultStructDef structDefId={structDefId} />)

    expect(screen.getByRole('link', { name: 'Struct<item-request-form,2>' })).toBeInTheDocument()
    expect(screen.getByText('The fields of this StructDef could not be loaded.')).toBeInTheDocument()
  })
})
