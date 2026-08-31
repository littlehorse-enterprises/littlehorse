#!/usr/bin/env node
/**
 * sdk-js's conformance testee (contract: conformance/README.md).
 *
 * BOOTSTRAP IMPLEMENTATION: master's sdk-js has no workflow-builder layer
 * yet, so each case is answered by constructing the PutWfSpecRequest
 * directly with the SDK's generated proto types and serializing it with the
 * SDK's own JSON writer. That proves the proto layer round-trips the canon —
 * not that a workflow builder emits it. When the wfsdk lands, each case body
 * here collapses to a builder call (e.g. wf.sleepSeconds(30)) and the green
 * cell starts meaning what it means for sdk-java.
 *
 * Build first: npm --prefix sdk-js run build
 */
const { PutWfSpecRequest } = require('../dist/proto/service.js')
const { ThreadSpec, Node, Edge, EntrypointNode, ExitNode, SleepNode } = require('../dist/proto/wf_spec.js')
const { VariableAssignment } = require('../dist/proto/common_wfspec.js')
const { VariableValue } = require('../dist/proto/type_definition.js')

const node = spec => Node.create(spec)
const edgeTo = sink => Edge.create({ sinkNodeName: sink })
const workflow = (caseId, nodes) =>
  PutWfSpecRequest.create({
    name: `probe-${caseId}`,
    threadSpecs: { entrypoint: ThreadSpec.create({ nodes }) },
    entrypointThreadName: 'entrypoint',
  })

/** Each case: variant 'base' | 'feature' → PutWfSpecRequest. */
const CASES = {
  'sleep-seconds': variant => {
    if (variant === 'base')
      return workflow('sleep-seconds', {
        '0-entrypoint-ENTRYPOINT': node({
          outgoingEdges: [edgeTo('1-exit-EXIT')],
          node: { oneofKind: 'entrypoint', entrypoint: EntrypointNode.create() },
        }),
        '1-exit-EXIT': node({ node: { oneofKind: 'exit', exit: ExitNode.create() } }),
      })
    return workflow('sleep-seconds', {
      '0-entrypoint-ENTRYPOINT': node({
        outgoingEdges: [edgeTo('1-sleep-SLEEP')],
        node: { oneofKind: 'entrypoint', entrypoint: EntrypointNode.create() },
      }),
      '1-sleep-SLEEP': node({
        outgoingEdges: [edgeTo('2-exit-EXIT')],
        node: {
          oneofKind: 'sleep',
          sleep: SleepNode.create({
            sleepLength: {
              oneofKind: 'rawSeconds',
              rawSeconds: VariableAssignment.create({
                source: {
                  oneofKind: 'literalValue',
                  literalValue: VariableValue.create({ value: { oneofKind: 'int', int: '30' } }),
                },
              }),
            },
          }),
        },
      }),
      '2-exit-EXIT': node({ node: { oneofKind: 'exit', exit: ExitNode.create() } }),
    })
  },
}

const args = process.argv.slice(2)
if (args[0] === 'list') {
  for (const id of Object.keys(CASES)) console.log(id)
  process.exit(0)
}
if (args[0] === 'compile' && args[1] === '--case' && args[3] === '--variant') {
  const [, , caseId, , variant] = args
  const build = CASES[caseId]
  if (!build) {
    console.error(`unknown case: ${caseId}`)
    process.exit(2)
  }
  if (!['base', 'feature'].includes(variant)) {
    console.error(`variant must be base|feature: ${variant}`)
    process.exit(2)
  }
  const json = PutWfSpecRequest.toJson(build(variant), { emitDefaultValues: true })
  console.log(JSON.stringify(json, null, 2))
  process.exit(0)
}
console.error('usage: testee list | compile --case ID --variant base|feature')
process.exit(2)
