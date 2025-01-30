import { formatDuration } from "@/app/utils";

export function Duration({ arrival, ended }: { arrival: string | undefined, ended: string | undefined }) {
    if (!arrival) return null

    const arrivalDate = new Date(arrival)
    const endedDate = new Date(ended ?? Date.now())
    const durationMs = endedDate.getTime() - arrivalDate.getTime()
    const durationDisplay = formatDuration(durationMs)

    const arrivalTime = arrivalDate.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false }) + `.${arrivalDate.getMilliseconds()}`;
    const endedTime = endedDate.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false }) + `.${endedDate.getMilliseconds()}`;
    const arrivalDay = arrivalDate.toLocaleDateString('en-US', { year: 'numeric', month: '2-digit', day: '2-digit' })
    const endedDay = endedDate.toLocaleDateString('en-US', { year: 'numeric', month: '2-digit', day: '2-digit' })

    return <div className="flex items-center justify-center gap-1">
        <p className="font-light text-[10px] text-center">Arrival: <br /> {arrivalDay} ({arrivalTime})</p>
        <div className="min-w-20 w-fit h-8 text-sm bg-blue-400 rounded-md items-center justify-center flex text-white">
            {ended ? durationDisplay : 'N/A'}
        </div>
        <p className="font-light text-[10px] text-center">Ended: <br /> {ended ? `${endedDay} (${endedTime})` : 'N/A'}</p>
    </div>
}
