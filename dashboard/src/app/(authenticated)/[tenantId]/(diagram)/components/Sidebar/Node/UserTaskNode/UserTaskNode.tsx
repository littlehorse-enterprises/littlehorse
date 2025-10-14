import {
  TaskDefId,
  UserTaskNode as UserTaskNodeProto,
  UTActionTrigger_UTACancel,
  UTActionTrigger_UTAReassign,
  UTActionTrigger_UTATask,
} from 'littlehorse-client/proto'
import { FC } from 'react'
import { VariableAssignment } from '../../Components'
import '../node.css'
import { InfoIcon } from 'lucide-react'
import { ActionTask } from './ActionTask'
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion'
export const UserTaskNode: FC<{ node: UserTaskNodeProto }> = ({ node }) => {
  const { userTaskDefName, userGroup, userId, notes, onCancellationExceptionName, userTaskDefVersion, actions } = node
  if (!node) return
  return (
    <div className="flex max-w-full flex-1 flex-col gap-2">
      <>
        <div className=" gap-1">
          <small className="node-title">UserTask </small>
          <p>{userTaskDefName}</p>
        </div>
        {userGroup && (
          <>
            <div className="flex items-center gap-1">
              <small className="node-title">User Group </small>
              <InfoIcon size={16} color="gray" />
            </div>
            <VariableAssignment variableAssigment={userGroup} />
          </>
        )}

        {userId && (
          <>
            <div className="flex items-center gap-1">
              <small className="node-title">User Id </small>
              <InfoIcon size={16} color="gray" />
            </div>
            <VariableAssignment variableAssigment={userId} />
          </>
        )}
        {userTaskDefVersion != undefined && (
          <>
            <div className="flex items-center gap-1">
              <small className="node-title">User Task Definition Version</small>
              <InfoIcon size={16} color="gray" />
            </div>
            <p>{userTaskDefVersion}</p>
          </>
        )}
        {notes && (
          <>
            <div className="flex items-center gap-1">
              <small className="node-title">Notes </small>
              <InfoIcon size={16} color="gray" />
            </div>
            <VariableAssignment variableAssigment={notes} />
          </>
        )}
        {onCancellationExceptionName && (
          <>
            <div className="flex items-center gap-1">
              <small className="node-title">Cancel Exception </small>
              <InfoIcon size={16} color="gray" />
            </div>
            <VariableAssignment variableAssigment={onCancellationExceptionName} />
          </>
        )}
        <div className="pb-4">
          <small className="node-title"> Actions</small>
          <div>
            {actions.map((action, index) => {
              const nodeType = action.action?.$case ?? ''
              return (
                <>
                  <Accordion type="single" collapsible>
                    <AccordionItem value={`action-${index}`}>
                      <AccordionTrigger>
                        <NodeTitleComponent title={nodeType} action={action.action?.value} />
                      </AccordionTrigger>
                      <AccordionContent>
                        <div className="flex mt-2"  key={`hook-content-${index}`}>
                          <p className="flex-none truncate bg-blue-500 px-2 font-mono text-gray-200">Hook</p>
                          <p className=" flex-grow truncate bg-black pl-2 font-mono text-gray-200">{action.hook}</p>
                        </div>
                        {action.delaySeconds && (
                          <div className="mt-2 flex" key={`action-content-${index}`}>
                            <p className="flex-none truncate bg-blue-500 px-2 font-mono text-gray-200">Delay</p>
                            <VariableAssignment variableAssigment={action.delaySeconds} />
                          </div>
                        )}
                        {nodeType === 'task' && <ActionTask node={action.action?.value as UTActionTrigger_UTATask} />}
                      </AccordionContent>
                    </AccordionItem>
                  </Accordion>
                </>
              )
            })}
          </div>
        </div>
      </>
    </div>
  )
}

export const NodeTitleComponent: FC<{
  title: string
  action: UTActionTrigger_UTATask | UTActionTrigger_UTACancel | UTActionTrigger_UTAReassign | undefined
}> = ({ title, action }) => {
  let summaryNodeTitle = ''
  if (title === 'task' && action && 'task' in action) {
    const value = action?.task?.taskToExecute?.value
    if (value && 'name' in value) {
      summaryNodeTitle = (value as TaskDefId).name ?? ''
    }
  }

  return (
    <div className="flex flex-1 items-center flex-start">
      <span className='truncate pl-1'>{title}</span>
      <span className="text-xs text-gray-500 ml-6">{summaryNodeTitle}</span>
    </div>
  )
}
