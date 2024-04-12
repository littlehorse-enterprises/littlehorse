import { SearchResult } from '../searchAction'

export * from './ExternalEventDefTable'
export * from './TaskDefTable'
export * from './UserTaskDefTable'
export * from './WfSpecTable'

export type SearchResultProps = {
  pages?: SearchResult[]
}
