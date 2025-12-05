import { render, screen } from '@testing-library/react'
import { AssignEvent } from '../AssignEvent'
import { UserTaskEvent_UTEAssigned } from 'littlehorse-client/proto'

describe('AssignEvent', () => {
  const baseEvent = {
    oldUserId: 'alice',
    newUserId: 'bob',
    oldUserGroup: 'groupA',
    newUserGroup: 'groupB',
  }

  it('Given users and groups it should render the Assignment ', () => {
    render(<AssignEvent event={baseEvent as UserTaskEvent_UTEAssigned} time={'2022-04-02T11:29:00Z'} />)
    expect(screen.getByText(/Assigned/i)).toBeInTheDocument()
    expect(screen.getByText('alice')).toBeInTheDocument()
    expect(screen.getByText('bob')).toBeInTheDocument()
    expect(screen.getByText('groupA')).toBeInTheDocument()
    expect(screen.getByText('groupB')).toBeInTheDocument()
    expect(screen.getByText(/April|2022|11:29/i)).toBeInTheDocument()
  })

  it('Given a new user with no old user it should render No assigned and the new user', () => {
    render(<AssignEvent event={{ oldUserId: undefined, newUserId: 'new user' } as UserTaskEvent_UTEAssigned} />)
    expect(screen.queryByText('No assigned')).toBeInTheDocument()
    expect(screen.queryByText('new user')).toBeInTheDocument()
  })

  it('Given a new group with no old group it should render No assigned and the new group', () => {
    render(<AssignEvent event={{ oldUserGroup: undefined, newUserGroup: 'new group' } as UserTaskEvent_UTEAssigned} />)
    expect(screen.queryByText('No assigned')).toBeInTheDocument()
    expect(screen.queryByText('new group')).toBeInTheDocument()
  })
  it('When there is no users neither user groups it should render nothing', () => {
    render(
      <AssignEvent
        event={
          {
            oldUserId: undefined,
            newUserId: undefined,
            oldUserGroup: undefined,
            newUserGroup: undefined,
          } as UserTaskEvent_UTEAssigned
        }
      />
    )
    expect(screen.queryByText('No assigned')).not.toBeInTheDocument()
  })
})
