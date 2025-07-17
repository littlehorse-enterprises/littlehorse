import { useContext } from 'react'
import { ThreadContext } from '../context'

export const useThread = () => {
  const { thread, setThread } = useContext(ThreadContext)
  return { thread, setThread }
}
