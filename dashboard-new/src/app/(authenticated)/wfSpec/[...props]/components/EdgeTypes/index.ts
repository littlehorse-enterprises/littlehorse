import { SmartStepEdge } from '@tisoap/react-flow-smart-edge'
import { EdgeTypes } from 'reactflow'
import { DefaultEdge } from './Default'

export const edgeTypes: EdgeTypes = {
  default: DefaultEdge,
  smart: SmartStepEdge,
}
