import { AccordionNode } from './AccordionContent'
import { WaitForChildWfNodeRun } from '../../Sidebar/NodeRunInfo/WaitForChildWfNodeRun'
import { FC } from 'react'

export const WaitForChildWfDefDetail: FC<AccordionNode<'waitForChildWf'>> = ({ nodeRun }) => (
  <WaitForChildWfNodeRun node={nodeRun.nodeType.value} />
)
