import { Dispatch, SetStateAction, createContext, useContext } from 'react'

export type ThreadType = {
  name: string
  number: number
}
type ThreadContextType = {
  thread: ThreadType
  setThread: Dispatch<SetStateAction<ThreadType>>
}
const ThreadContext = createContext<ThreadContextType>({ thread: { name: '', number: 0 }, setThread: () => {} })

export const ThreadProvider = ThreadContext.Provider

export const useThread = () => {
  const { thread, setThread } = useContext(ThreadContext)
  return { thread, setThread }
}
