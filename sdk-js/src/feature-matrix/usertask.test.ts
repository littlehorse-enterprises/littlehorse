/**
 * Feature matrix: user task schemas.
 *
 * See sdk-js/PARITY_PLAN.md. Java derives UserTaskDef schemas from annotated
 * classes (usertask/UserTaskSchema, @UserTaskField); the JS equivalent should
 * lean on the ecosystem (e.g. zod, as the worker structs already do) while
 * producing the same PutUserTaskDefRequest proto.
 */

describe('usertask', () => {
  test.todo('define a user task schema from a typed object definition — Java: UserTaskSchema')
  test.todo('compile the schema to a PutUserTaskDefRequest — Java: UserTaskSchema#compile')
  test.todo('map field types, display names, descriptions, and required flags — Java: @UserTaskField')
  test.todo('register the UserTaskDef with the server — Java: UserTaskSchema + client')
})
