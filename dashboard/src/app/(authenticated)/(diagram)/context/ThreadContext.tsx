import { Dispatch, SetStateAction, createContext } from 'react'

export type ThreadType = {
  name: string
  number: number
}
type ThreadContextType = {
  thread: ThreadType
  setThread: Dispatch<SetStateAction<ThreadType>>
}
export const ThreadContext = createContext<ThreadContextType>({ thread: { name: '', number: 0 }, setThread: () => {} })

export const ThreadProvider = ThreadContext.Provider
