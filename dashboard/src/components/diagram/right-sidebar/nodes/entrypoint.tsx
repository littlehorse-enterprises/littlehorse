import { NodeRun, EntrypointRun } from "littlehorse-client/proto";
import { Section } from "../section";
import { NodeForType } from "@/utils/data/node";

export function EntrypointNodeComponent({ }: NodeForType<'ENTRYPOINT'>) {
  return (
    <Section title="EntrypointNode" />
  )
}

interface EntrypointNodeRunComponentProps {
  entrypointNodeRun: NodeRun & { entrypoint: EntrypointRun }
}

export function EntrypointNodeRunComponent({ }: EntrypointNodeRunComponentProps) {
  return (
    <div>
      <h4 className="mb-2 text-xs font-medium">Entrypoint Run</h4>
    </div>
  )
}
