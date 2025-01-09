import { FC } from 'react'
import { NodeRun } from 'littlehorse-client/proto'
import { useQuery } from '@tanstack/react-query'
import { utcToLocalDateTime } from '@/app/utils'
import { cn } from '@/components/utils'
import { getWaitForCondition } from '../../NodeTypes/WaitForCondition/getWaitForCondition'
import { ClipboardIcon, RefreshCwIcon } from 'lucide-react'
import { useParams } from 'next/navigation'

export const WaitForConditionDefDetail: FC<{ nodeRun: NodeRun }> = ({ nodeRun }) => {
  // ? Commented out, but here incase we need it later.
  // const { tenantId } = useParams() as { tenantId: string }

  // const { data, isLoading, error } = useQuery({
  //     queryKey: ['waitForCondition', nodeRun.wfSpecId, nodeRun.nodeName],
  //     queryFn: async () => {
  //         if (!nodeRun.wfSpecId || !nodeRun.nodeName) return
  //         return await getWaitForCondition({ tenantId, nodeRun })
  //     },
  // })

  // if (isLoading) {
  //     return (
  //         <div className="flex min-h-[60px] items-center justify-center text-center">
  //             <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
  //         </div>
  //     )
  // }

  // if (error) {
  //     return <div>Error loading data</div>
  // }

  // if (!data) return null

  return <div className="mb-2 items-center gap-2">{/* {JSON.stringify(data)} */}</div>
}
