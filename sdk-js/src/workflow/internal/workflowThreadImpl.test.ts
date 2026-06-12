import { VariableMutationType } from '../../proto/common_wfspec'
import { LHMisconfigurationException } from '../exceptions'
import { Workflow } from '../workflow'
import { WorkflowThreadImpl } from './workflowThreadImpl'

describe('WorkflowThreadImpl', () => {
  it('rejects duplicate variable names', () => {
    expect(() =>
      Workflow.newWorkflow('dup', thread => {
        thread.declareStr('x')
        thread.declareStr('x')
        thread.complete()
      }).compile()
    ).toThrow(LHMisconfigurationException)
  })

  it('rejects steps after complete()', () => {
    expect(() =>
      Workflow.newWorkflow('after', thread => {
        thread.complete()
        thread.execute('t')
      }).compile()
    ).toThrow(LHMisconfigurationException)
  })

  it('mutate rejects non-thread variables', () => {
    expect(() =>
      Workflow.newWorkflow('bad-mutate', thread => {
        ;(thread as unknown as WorkflowThreadImpl).mutate({ name: 'x' } as never, VariableMutationType.ASSIGN, 1)
        thread.complete()
      }).compile()
    ).toThrow(LHMisconfigurationException)
  })
})
