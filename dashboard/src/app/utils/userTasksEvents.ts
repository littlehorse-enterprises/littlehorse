import { UserTaskEvent } from 'littlehorse-client/proto'

/**
 * Type guard to check if a UserTaskEvent has a specific event case.
 *
 * @param caseName - The name of the event case to check for.
 * @returns A type guard function that checks if the UserTaskEvent has the specified event case.
 */
export function hasEventCase<
  C extends UserTaskEvent['event'] extends infer U ? (U extends { $case: string } ? U['$case'] : never) : never,
>(caseName: C) {
  return (
    e: UserTaskEvent
  ): e is Omit<UserTaskEvent, 'event'> & { event: Extract<NonNullable<UserTaskEvent['event']>, { $case: C }> } =>
    !!e.event && e.event.$case === caseName
}
