import { WfRunId } from 'littlehorse-client/proto'
import { concatWfRunIds } from '.'

describe('concatWfRunIds', () => {
  it('should return reverse relationship', () => {
    const wfRunId: WfRunId = {
      id: 'child',
      parentWfRunId: {
        id: 'parent',
      },
    }
    expect(concatWfRunIds(wfRunId)).toEqual('parent/child')
  })

  it('should return stacked reverse relationship', () => {
    const wfRunId: WfRunId = {
      id: 'child',
      parentWfRunId: {
        id: 'parent',
        parentWfRunId: {
          id: 'grandparent',
          parentWfRunId: {
            id: 'great-grandparent',
          },
        },
      },
    }
    expect(concatWfRunIds(wfRunId)).toEqual('great-grandparent/grandparent/parent/child')
  })
})
