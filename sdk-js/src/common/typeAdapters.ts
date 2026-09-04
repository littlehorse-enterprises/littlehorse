import { VariableValue } from '../proto/type_definition'

/**
 * Serde for a type the SDK does not know natively (Java: LHTypeAdapter).
 *
 * Without this, a custom class such as a `Money` or a `Decimal` falls through
 * to the generic object branch and is stored as JSON, losing its identity on
 * the way back. An adapter lets a user decide the encoding.
 *
 * JS has no runtime type token to dispatch on, so unlike Java the adapter
 * declares both directions explicitly: `matches` for serialization, and
 * `name` so a caller can ask for a specific adapter when reading back.
 */
export interface LHTypeAdapter<T = unknown> {
  /** Identifies the adapter when deserializing. */
  name: string
  /** True when this adapter should encode `value`. */
  matches(value: unknown): boolean
  serialize(value: T): VariableValue
  deserialize(value: VariableValue): T
}

/**
 * The set of adapters a config makes available to the wfsdk and the worker
 * (Java: LHTypeAdapterRegistry).
 */
export class LHTypeAdapterRegistry {
  private readonly adapters: LHTypeAdapter[] = []

  static empty(): LHTypeAdapterRegistry {
    return new LHTypeAdapterRegistry()
  }

  add(adapter: LHTypeAdapter): this {
    if (this.adapters.some(existing => existing.name === adapter.name)) {
      throw new Error(`A type adapter named '${adapter.name}' is already registered`)
    }
    this.adapters.push(adapter)
    return this
  }

  /** Adapters in registration order. */
  list(): readonly LHTypeAdapter[] {
    return this.adapters
  }

  get size(): number {
    return this.adapters.length
  }

  byName(name: string): LHTypeAdapter | undefined {
    return this.adapters.find(adapter => adapter.name === name)
  }

  /** First adapter claiming this value; undefined means use the built-ins. */
  findForValue(value: unknown): LHTypeAdapter | undefined {
    return this.adapters.find(adapter => adapter.matches(value))
  }

  /**
   * Adapters are opt-in on the way back: an encoded value carries no marker
   * saying which adapter produced it, so decoding stays with the built-ins
   * unless a caller asks for an adapter by name. Overriding this would mean
   * guessing, and guessing wrong silently returns the wrong type.
   */
  findForVariableValue(_value: VariableValue): LHTypeAdapter | undefined {
    return undefined
  }
}
