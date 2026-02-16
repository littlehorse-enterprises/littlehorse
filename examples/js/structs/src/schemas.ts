import { lhStruct, lh, Infer } from 'littlehorse-client'

// ── Struct schemas ───────────────────────────────────────────────────

export const Address = lhStruct('address', {
  houseNumber: lh.INT,
  street: lh.STR,
  city: lh.STR,
  state: lh.STR,
  zip: lh.INT,
})
export type Address = Infer<typeof Address>

export const Person = lhStruct('person', {
  firstName: lh.STR,
  lastName: lh.STR,
  homeAddress: lh.struct(Address),
})
export type Person = Infer<typeof Person>

export const ParkingTicketReport = lhStruct('parking-ticket-report', {
  vehicleMake: lh.STR,
  vehicleModel: lh.STR,
  licensePlateNumber: lh.STR,
  reportedAt: lh.STR,
})
export type ParkingTicketReport = Infer<typeof ParkingTicketReport>
