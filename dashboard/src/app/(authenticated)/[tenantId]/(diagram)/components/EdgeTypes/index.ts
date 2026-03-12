import { EdgeTypes } from 'reactflow'
import CustomEdge from './Default'
import { extractEdges } from './extractEdges'

export { extractEdges }

const edgeTypes: EdgeTypes = {
  custom: CustomEdge,
}

export default edgeTypes
