import { UserTaskNode as UserTaskNodeProto, UTActionTrigger_UTATask } from 'littlehorse-client/proto'
import { FC } from 'react'
import { VariableAssignment } from '../../Components'
import '../node.css'
import { InfoIcon } from 'lucide-react'
import { ActionTask } from './ActionTask'
import { Accordion, Content, Header, Item, Trigger } from '@radix-ui/react-accordion'
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
        <div>
          <small className="node-title"> Actions</small>
          <div>
            {actions.map((action, index) => (
              <>
                <Accordion type="single" collapsible>
                  <Item value={`action-${index}`}>
                    <Header>
                      <Trigger>{action.action?.$case.toString()}</Trigger>
                    </Header>
                    <Content>
                      <div className="flex" key={`hook-content-${index}`}>
                        <p className="flex-none truncate bg-blue-500 px-2 font-mono text-gray-200">Hook</p>
                        <p className=" flex-grow truncate bg-black pl-2 font-mono text-gray-200">{action.hook}</p>
                      </div>
                      {action.delaySeconds && (
                        <div className="mt-1 flex" key={`action-content-${index}`}>
                          <p className="flex-none truncate bg-blue-500 px-2 font-mono text-gray-200">Delay</p>
                          <VariableAssignment variableAssigment={action.delaySeconds} />
                        </div>
                      )}
                      {action.action?.$case.toString() === 'task' && <ActionTask node={action.action?.value as UTActionTrigger_UTATask} />}
                    </Content>
                  </Item>
                </Accordion>
              </>
            ))}
          </div>
        </div>
      </>
    </div>
  )
}
