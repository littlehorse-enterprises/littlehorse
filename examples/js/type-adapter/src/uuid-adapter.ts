import { randomUUID } from 'crypto'
import type { LHTypeAdapter } from 'littlehorse-client'
import type { VariableValue } from 'littlehorse-client/proto'

const UUID_FORMAT = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i

// JS has no java.util.UUID; a small class gives the adapter a real type to
// dispatch on (adapters match values, not compile-time types).
export class UUID {
  private constructor(private readonly value: string) {}

  static randomUUID(): UUID {
    return new UUID(randomUUID())
  }

  static fromString(src: string): UUID {
    if (!UUID_FORMAT.test(src)) throw new Error(`Invalid UUID string: ${src}`)
    return new UUID(src)
  }

  toString(): string {
    return this.value
  }
}

// Mirrors Java's UUIDTypeAdapter (an LHStringAdapter<UUID>): UUID -> STR when
// serializing, STR -> UUID when a caller asks for this adapter by name.
export class UUIDTypeAdapter implements LHTypeAdapter<UUID> {
  name = 'uuid'

  matches(value: unknown): boolean {
    return value instanceof UUID
  }

  serialize(value: UUID): VariableValue {
    return { value: { oneofKind: 'str', str: value.toString() } }
  }

  deserialize(value: VariableValue): UUID {
    if (value.value.oneofKind !== 'str') throw new Error('Expected a STR VariableValue for a UUID')
    return UUID.fromString(value.value.str)
  }
}
