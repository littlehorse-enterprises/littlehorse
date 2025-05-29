/* --------------------------------- Search --------------------------------- */
export const SEARCH_LIMITS = [10, 20, 30, 60, 100] as const;
export const SEARCH_LIMIT_DEFAULT: (typeof SEARCH_LIMITS)[number] = 10;
export const SEARCH_ENTITIES = [
  "WfSpec",
  "TaskDef",
  "UserTaskDef",
  "ExternalEventDef",
  "WorkflowEventDef",
] as const;
