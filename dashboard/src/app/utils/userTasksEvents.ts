import { UserTaskEvent } from 'littlehorse-client/proto'

/**
 * Type guard to check if a UserTaskEvent has a specific event case.
 *
 * @param caseName - The name of the event case to check for.
 * @returns A type guard function that checks if the UserTaskEvent has the specified event case.
 */
export function hasEventCase<
  C extends UserTaskEvent['event'] extends infer U ? (U extends { oneofKind: string } ? U['oneofKind'] : never) : never,
>(caseName: C) {
  return (
    e: UserTaskEvent
  ): e is Omit<UserTaskEvent, 'event'> & { event: Extract<NonNullable<UserTaskEvent['event']>, { oneofKind: C }> } =>
    !!e.event && e.event.oneofKind === caseName
}
