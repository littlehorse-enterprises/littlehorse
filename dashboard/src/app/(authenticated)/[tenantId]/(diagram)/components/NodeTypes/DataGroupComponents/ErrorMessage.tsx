import { OverflowText } from '@/app/(authenticated)/[tenantId]/components/OverflowText'
import { cn } from '@/components/utils'
export function ErrorMessage({ errorMessage }: { errorMessage: string | undefined }) {
  return (
    <div
      className={cn('w-full rounded-lg border border-black bg-gray-300 py-1 text-center text-xs', {
        'bg-red-300 text-red-500': !!errorMessage,
      })}
    >
      <OverflowText variant="error" className="px-2 py-0" text={errorMessage ?? 'NO ERROR MESSAGE'} />
    </div>
  )
}
