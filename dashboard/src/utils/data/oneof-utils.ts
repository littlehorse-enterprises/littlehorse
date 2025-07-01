/** Extracts all the case names from a oneOf field. */
export type OneOfCases<T> = T extends { $case: infer U extends string } ? U : never

/** Extracts a union of all the value types from a oneOf field */
export type OneOfValues<T> = T extends { $case: infer U extends string; [key: string]: unknown } ? T[U] : never

/** Extracts the specific type of a oneOf case based on its field name */
export type OneOfCase<T, K extends OneOfCases<T>> = T extends {
  $case: K
  [key: string]: unknown
}
  ? T
  : never

/** Extracts the specific type of a value type from a oneOf field */
export type OneOfValue<T, K extends OneOfCases<T>> = T extends {
  $case: infer U extends K
  [key: string]: unknown
}
  ? T[U]
  : never
