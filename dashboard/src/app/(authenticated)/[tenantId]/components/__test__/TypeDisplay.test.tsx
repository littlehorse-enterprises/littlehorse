import { fireEvent, render, screen, within } from '@testing-library/react'
import { TypeDefinition, VariableType, VariableValue } from 'littlehorse-client/proto'
import { TypeDisplay } from '../TypeDisplay'

jest.mock('../LinkWithTenant', () => ({
  __esModule: true,
  default: ({ href, children, ...props }: React.ComponentProps<'a'>) => (
    <a href={href} {...props}>
      {children}
    </a>
  ),
}))

describe('TypeDisplay', () => {
  it('shows a recursive InlineStruct schema in one dialog', () => {
    const definedType: TypeDefinition['definedType'] = {
      oneofKind: 'inlineStructDef',
      inlineStructDef: {
        fields: {
          name: {
            fieldType: {
              definedType: { oneofKind: 'primitiveType', primitiveType: VariableType.STR },
              masked: true,
            },
            isNullable: false,
            description: 'Customer name',
          },
          address: {
            fieldType: {
              definedType: {
                oneofKind: 'inlineStructDef',
                inlineStructDef: {
                  fields: {
                    city: {
                      fieldType: {
                        definedType: { oneofKind: 'primitiveType', primitiveType: VariableType.STR },
                        masked: false,
                      },
                      defaultValue: VariableValue.create({ value: { oneofKind: 'str', str: 'Chicago' } }),
                      isNullable: false,
                    },
                  },
                },
              },
              masked: false,
            },
            isNullable: true,
          },
          previousAddresses: {
            fieldType: {
              definedType: {
                oneofKind: 'inlineArrayDef',
                inlineArrayDef: {
                  arrayType: {
                    definedType: {
                      oneofKind: 'inlineStructDef',
                      inlineStructDef: {
                        fields: {
                          postalCode: {
                            fieldType: {
                              definedType: { oneofKind: 'primitiveType', primitiveType: VariableType.STR },
                              masked: false,
                            },
                            isNullable: false,
                          },
                        },
                      },
                    },
                    masked: false,
                  },
                },
              },
              masked: false,
            },
            isNullable: false,
          },
          account: {
            fieldType: {
              definedType: {
                oneofKind: 'structDefId',
                structDefId: { name: 'account', version: 2 },
              },
              masked: false,
            },
            isNullable: false,
          },
        },
      },
    }

    render(<TypeDisplay definedType={definedType} />)
    const inspectButton = screen.getByRole('button', { name: 'Inspect InlineStruct schema' })
    expect(inspectButton.querySelector('svg')).toBeInTheDocument()
    fireEvent.click(inspectButton)

    const dialog = screen.getByRole('dialog')
    expect(screen.getByRole('heading', { name: 'InlineStruct schema' })).toBeInTheDocument()
    expect(screen.getByText('name')).toBeInTheDocument()
    expect(screen.getByText('Customer name')).toBeInTheDocument()
    expect(screen.getByText('Masked')).toBeInTheDocument()
    expect(screen.getByText('address')).toBeInTheDocument()
    expect(screen.getByText('Nullable')).toBeInTheDocument()
    expect(within(dialog).queryByRole('button', { name: 'Inspect InlineStruct schema' })).not.toBeInTheDocument()
    expect(screen.getAllByText('Nested fields')).toHaveLength(2)
    expect(screen.getByText('Default: Chicago')).toBeInTheDocument()
    expect(screen.getByText('postalCode')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Struct<account,2>' })).toHaveAttribute('href', '/structDef/account/2')
  })
})
