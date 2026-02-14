import { LHStruct, LHField } from 'littlehorse-client'
import { VariableType } from 'littlehorse-client/proto'
import { Address } from './address.js'

@LHStruct({ name: 'person' })
export class Person {
  @LHField(VariableType.STR)
  firstName!: string

  @LHField(VariableType.STR)
  lastName!: string

  @LHField({ struct: Address })
  homeAddress!: Address

  constructor(firstName?: string, lastName?: string, homeAddress?: Address) {
    if (firstName !== undefined) this.firstName = firstName
    if (lastName !== undefined) this.lastName = lastName
    if (homeAddress !== undefined) this.homeAddress = homeAddress
  }

  toString(): string {
    return `${this.firstName} ${this.lastName}`
  }
}
