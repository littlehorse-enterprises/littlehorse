import { UserTaskEvent } from "littlehorse-client/proto";

export function hasEventCase<
  C extends UserTaskEvent['event'] extends infer U ? (U extends { $case: string } ? U['$case'] : never) : never,
>(caseName: C) {
  return (
    e: UserTaskEvent
  ): e is Omit<UserTaskEvent, 'event'> & { event: Extract<NonNullable<UserTaskEvent['event']>, { $case: C }> } =>
    !!e.event && e.event.$case === caseName
}
