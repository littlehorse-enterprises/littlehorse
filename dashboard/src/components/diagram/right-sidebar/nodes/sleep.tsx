import { Node, SleepNode } from "littlehorse-client/proto";

interface SleepNodeComponentProps {
  sleepNode: Node & { sleep: SleepNode }
}

export function SleepNodeComponent({ sleepNode }: SleepNodeComponentProps) {
  return (
    <div className="space-y-4">
      <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
        <h4 className="mb-2 text-xs font-medium">Sleep Properties</h4>
        <div className="space-y-1 text-xs">
          <div className="flex justify-between">
            <span className="text-[#656565]">Type:</span>
            <span className="font-mono">SLEEP</span>
          </div>
          {sleepNode.sleep.rawSeconds && (
            <div className="flex justify-between">
              <span className="text-[#656565]">Duration:</span>
              <span className="font-mono">{JSON.stringify(sleepNode.sleep.rawSeconds)} seconds</span>
            </div>
          )}
          {sleepNode.sleep.timestamp && (
            <div className="flex justify-between">
              <span className="text-[#656565]">Until Timestamp:</span>
              <span className="font-mono">{JSON.stringify(sleepNode.sleep.timestamp)}</span>
            </div>
          )}
          {sleepNode.sleep.isoDate && (
            <div className="flex justify-between">
              <span className="text-[#656565]">Until Date:</span>
              <span className="font-mono">{JSON.stringify(sleepNode.sleep.isoDate)}</span>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
