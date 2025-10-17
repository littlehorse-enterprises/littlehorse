import { SearchResult } from '../searchAction'

export * from './ExternalEventDefTable'
export * from './StructDefTable'
export * from './TaskDefTable'
export * from './UserTaskDefTable'
export * from './WfSpecTable'
export * from './WorkflowEventDefTable'

export type SearchResultProps = {
  pages?: SearchResult[]
}
