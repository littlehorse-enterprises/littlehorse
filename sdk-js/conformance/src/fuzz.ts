/**
 * Random dual-compile generator — implements the normative contract in
 * conformance/FUZZ.md exactly (PRNG, draw order, op table). No canon: the
 * runner cross-compares SDK outputs for the same seed.
 */
import { Workflow, type WfRunVariable } from '../../dist/wfsdk'
import { PutWfSpecRequest } from '../../dist/proto/service'
import { Comparator } from '../../dist/proto/type_definition'
import { VariableMutationType } from '../../dist/proto/common_wfspec'

function mulberry32(seed: number) {
  let a = seed | 0
  return (bound: number): number => {
    a = (a + 0x6d2b79f5) | 0
    let t = a ^ (a >>> 15)
    t = Math.imul(t, 1 | a)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    const draw = (t ^ (t >>> 14)) >>> 0
    return draw % bound
  }
}

export function compile(seed: number, ops: number): string {
  const nextInt = mulberry32(seed)
  const wf = Workflow.newWorkflow(`fuzz-${seed}`, thread => {
    const intVars: WfRunVariable[] = []
    for (let i = 0; i < ops; i++) {
      const k = nextInt(8)
      switch (k) {
        case 0:
          intVars.push(thread.declareInt(`v${i}`))
          break
        case 1:
          thread.declareStr(`v${i}`)
          break
        case 2:
          thread.declareBool(`v${i}`)
          break
        case 3:
          thread.execute(`task-${nextInt(5)}`)
          break
        case 4:
          thread.sleepSeconds(1 + nextInt(60))
          break
        case 5:
          thread.waitForEvent(`evt-${nextInt(5)}`)
          break
        case 6:
          if (intVars.length === 0) intVars.push(thread.declareInt(`v${i}`))
          else thread.mutate(intVars[nextInt(intVars.length)], VariableMutationType.ADD, nextInt(10))
          break
        case 7:
          if (intVars.length === 0) {
            thread.execute(`task-${nextInt(5)}`)
          } else {
            const v = intVars[nextInt(intVars.length)]
            const rhs = nextInt(10)
            const branch = nextInt(5)
            thread.doIf(thread.condition(v, Comparator.GREATER_THAN, rhs), body => body.execute(`branch-${branch}`))
          }
          break
      }
    }
  })
  return JSON.stringify(PutWfSpecRequest.toJson(wf.compileWorkflow(), { emitDefaultValues: true }), null, 2)
}
