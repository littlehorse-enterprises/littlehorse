import { formatDuration } from '@/app/utils'

export function Duration({ arrival, ended }: { arrival: string | undefined; ended: string | undefined }) {
  if (!arrival) return null

  const arrivalDate = new Date(arrival)
  const endedDate = new Date(ended ?? Date.now())
  const durationMs = endedDate.getTime() - arrivalDate.getTime()
  const durationDisplay = formatDuration(durationMs)

  const arrivalTime =
    arrivalDate.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false }) +
    `.${arrivalDate.getMilliseconds()}`
  const endedTime =
    endedDate.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false }) +
    `.${endedDate.getMilliseconds()}`
  const arrivalDay = arrivalDate.toLocaleDateString('en-US', { year: 'numeric', month: '2-digit', day: '2-digit' })
  const endedDay = endedDate.toLocaleDateString('en-US', { year: 'numeric', month: '2-digit', day: '2-digit' })

  return (
    <div className="flex items-center justify-center gap-1 px-1">
      <p className="text-center text-[10px] font-light">
        Arrival: <br /> {arrivalDay} ({arrivalTime})
      </p>
      <div className="flex h-8 w-fit min-w-20 items-center justify-center rounded-md bg-blue-400 text-sm text-white">
        {ended ? durationDisplay : 'N/A'}
      </div>
      <p className="text-center text-[10px] font-light">
        Ended: <br /> {ended ? `${endedDay} (${endedTime})` : 'N/A'}
      </p>
    </div>
  )
}
