import { z } from 'zod'
import { lhStruct } from 'littlehorse-client'

export const Address = lhStruct(
  'address',
  z.object({
    street: z.string(),
    city: z.string(),
    state: z.string(),
    zip: z.number().int(),
  })
)
export type Address = z.infer<typeof Address>

export const Person = lhStruct(
  'person',
  z.object({
    name: z.string(),
    email: z.string(),
    address: Address,
  })
)
export type Person = z.infer<typeof Person>
