import { z } from 'zod'
import { lhStruct } from 'littlehorse-client'

// ── Struct schemas ───────────────────────────────────────────────────

export const Address = lhStruct(
  'address',
  z.object({
    houseNumber: z.number().int(),
    street: z.string(),
    city: z.string(),
    state: z.string(),
    zip: z.number().int(),
  }),
)
export type Address = z.infer<typeof Address>

export const Person = lhStruct(
  'person',
  z.object({
    firstName: z.string(),
    lastName: z.string(),
    homeAddress: Address,
  }),
)
export type Person = z.infer<typeof Person>

export const ParkingTicketReport = lhStruct(
  'parking-ticket-report',
  z.object({
    vehicleMake: z.string(),
    vehicleModel: z.string(),
    licensePlateNumber: z.string(),
    reportedAt: z.string(),
  }),
)
export type ParkingTicketReport = z.infer<typeof ParkingTicketReport>
