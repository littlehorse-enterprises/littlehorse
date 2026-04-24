export class LHMisconfigurationException extends Error {
  override readonly name = 'LHMisconfigurationException'

  constructor(message: string) {
    super(message)
    Object.setPrototypeOf(this, LHMisconfigurationException.prototype)
  }
}
