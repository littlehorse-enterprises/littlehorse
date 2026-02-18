import { AccordionNode } from './AccordionContent'
import { ChildWFNodeRun } from '../../Sidebar/NodeRunInfo/ChildWFNodeRun'
import { FC } from 'react'

export const RunChildWfDefDetail: FC<AccordionNode<'runChildWf'>> = ({ nodeRun }) => (
  <ChildWFNodeRun node={nodeRun.nodeType.value} />
)
