/**
 * Typed errors mirroring the Java SDK's exception taxonomy
 * (Java: io.littlehorse.sdk.common.exception).
 *
 * The point of distinct classes is that callers can branch on the *kind* of
 * failure: a serde problem is a bug in the data, a misconfiguration is a bug
 * in the setup, and a task exception is business logic doing its job.
 */

/** Base class, so `catch (e) { if (e instanceof LHError) ... }` works. */
export class LHError extends Error {
  constructor(message: string) {
    super(message)
    this.name = new.target.name
  }
}

/** A value could not be converted to or from a VariableValue. */
export class LHSerdeError extends LHError {}

/** The SDK was configured in a way that cannot work. */
export class LHMisconfigurationError extends LHError {}

/** A task's input variables could not be mapped to its arguments. */
export class InputVarSubstitutionError extends LHError {}
