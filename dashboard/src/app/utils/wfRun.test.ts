import { WfRunId } from 'littlehorse-client/proto'
import { flattenWfRunId, wfRunIdFromFlattenedId, wfRunIdFromList, wfRunIdToPath } from '.'

describe('wfRunIdToPath', () => {
  it('should return reverse relationship', () => {
    const wfRunId: WfRunId = {
      id: 'child',
      parentWfRunId: {
        id: 'parent',
      },
    }
    expect(wfRunIdToPath(wfRunId)).toEqual('parent/child')
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
    expect(wfRunIdToPath(wfRunId)).toEqual('great-grandparent/grandparent/parent/child')
  })
})

describe('wfRunIdFromFlattenedId', () => {
  it('should return a single id', () => {
    const flattenedId = 'single'
    expect(wfRunIdFromFlattenedId(flattenedId)).toEqual({ id: 'single', parentWfRunId: undefined })
  })

  it('should return a parent-child relationship', () => {
    const flattenedId = 'parent_child'
    expect(wfRunIdFromFlattenedId(flattenedId)).toEqual({
      id: 'child',
      parentWfRunId: { id: 'parent', parentWfRunId: undefined },
    })
  })

  it('should return a stacked parent-child relationship', () => {
    const flattenedId = 'great-grandparent_grandparent_parent_child'
    expect(wfRunIdFromFlattenedId(flattenedId)).toEqual({
      id: 'child',
      parentWfRunId: {
        id: 'parent',
        parentWfRunId: {
          id: 'grandparent',
          parentWfRunId: { id: 'great-grandparent', parentWfRunId: undefined },
        },
      },
    })
  })
})

describe('flattenWfRunId', () => {
  it('should return a single id', () => {
    const wfRunId: WfRunId = { id: 'single' }
    expect(flattenWfRunId(wfRunId)).toEqual('single')
  })

  it('should return a parent-child relationship', () => {
    const wfRunId: WfRunId = {
      id: 'child',
      parentWfRunId: { id: 'parent' },
    }
    expect(flattenWfRunId(wfRunId)).toEqual('parent_child')
  })

  it('should return a stacked parent-child relationship', () => {
    const wfRunId: WfRunId = {
      id: 'child',
      parentWfRunId: {
        id: 'parent',
        parentWfRunId: {
          id: 'grandparent',
          parentWfRunId: { id: 'great-grandparent' },
        },
      },
    }
    expect(flattenWfRunId(wfRunId)).toEqual('great-grandparent_grandparent_parent_child')
  })
})

describe('wfRunIdFromList', () => {
  it('should return id for parent/child', async () => {
    const ids = ['parent', 'child']

    expect(wfRunIdFromList(ids)).toEqual({
      id: 'parent',
      parentWfRunId: { id: 'child', parentWfRunId: undefined },
    })
  })

  it('should return wfRunId for grant-parent/parent/child', async () => {
    const ids = ['grand-parent', 'parent', 'child']
    expect(wfRunIdFromList(ids)).toEqual({
      id: 'grand-parent',
      parentWfRunId: { id: 'parent', parentWfRunId: { id: 'child', parentWfRunId: undefined } },
    })
  })
})
