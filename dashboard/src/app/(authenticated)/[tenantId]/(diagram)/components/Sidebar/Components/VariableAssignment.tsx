import { getVariable } from '@/app/utils'
import { VariableAssignment as VariableAssignmentProto } from 'littlehorse-client/proto'
import { FC, useCallback } from 'react'
import { useModal } from '../../../hooks/useModal'

export const VariableAssignment: FC<{ variableAssigment: VariableAssignmentProto }> = ({ variableAssigment }) => {
  const variable = getVariable(variableAssigment)
  const { setModal, setShowModal } = useModal()

  const onClick = useCallback(() => {
    setModal({ type: 'variableAssignment', data: variableAssigment })
    setShowModal(true)
  }, [setModal, setShowModal])

  return (
    <p className="flex-grow cursor-pointer truncate bg-black pl-2 font-mono text-gray-200" onClick={onClick}>
      {variable}
    </p>
  )
}
