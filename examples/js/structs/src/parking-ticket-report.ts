import { LHStruct, LHField } from 'littlehorse-client'
import { VariableType } from 'littlehorse-client/proto'

@LHStruct({ name: 'car' })
export class ParkingTicketReport {
  @LHField(VariableType.STR)
  vehicleMake!: string

  @LHField(VariableType.STR)
  vehicleModel!: string

  @LHField(VariableType.STR)
  licensePlateNumber!: string

  @LHField(VariableType.STR)
  createdAt!: string

  constructor(vehicleMake?: string, vehicleModel?: string, licensePlateNumber?: string, createdAt?: string) {
    if (vehicleMake !== undefined) this.vehicleMake = vehicleMake
    if (vehicleModel !== undefined) this.vehicleModel = vehicleModel
    if (licensePlateNumber !== undefined) this.licensePlateNumber = licensePlateNumber
    if (createdAt !== undefined) this.createdAt = createdAt
  }

  toString(): string {
    return `${this.vehicleMake} ${this.vehicleModel}, Plate Number: ${this.licensePlateNumber}, issued at ${this.createdAt}`
  }
}
