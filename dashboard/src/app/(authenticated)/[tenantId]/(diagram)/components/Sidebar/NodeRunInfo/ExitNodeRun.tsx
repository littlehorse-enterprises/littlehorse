import { getVariableValue } from '@/app/utils'
import { FC } from 'react'
import { useDiagram } from '../../../hooks/useDiagram'
import { NodeVariable } from '../Components/NodeVariable'

export const ExitNodeRun: FC = () => {
  const { thread, wfRun } = useDiagram()

  const threadRun = wfRun?.threadRuns.find(tr => tr.number === thread.number)
  const output = threadRun?.output

  return <NodeVariable label="Return output:" text={output ? getVariableValue(output) : 'N/A'} />
}
