import { LHStruct, LHField } from 'littlehorse-client'
import { VariableType } from 'littlehorse-client/proto'

@LHStruct({ name: 'address' })
export class Address {
  @LHField(VariableType.INT)
  houseNumber!: number

  @LHField(VariableType.STR)
  street!: string

  @LHField(VariableType.STR)
  city!: string

  @LHField(VariableType.STR)
  planet!: string

  @LHField(VariableType.INT)
  zipCode!: number

  constructor(houseNumber?: number, street?: string, city?: string, planet?: string, zipCode?: number) {
    if (houseNumber !== undefined) this.houseNumber = houseNumber
    if (street !== undefined) this.street = street
    if (city !== undefined) this.city = city
    if (planet !== undefined) this.planet = planet
    if (zipCode !== undefined) this.zipCode = zipCode
  }

  toString(): string {
    return `${this.houseNumber} ${this.street}, ${this.city}, ${this.planet} ${this.zipCode}`
  }
}
