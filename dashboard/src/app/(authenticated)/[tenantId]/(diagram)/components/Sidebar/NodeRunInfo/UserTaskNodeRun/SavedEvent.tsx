import { formatDateReadable } from "@/app/utils";
import { UserTaskEvent_UTESaved } from "littlehorse-client/proto";

export const SavedEvent =({ event, time }: { event: UserTaskEvent_UTESaved; time?: string }) =>{
    return (
        <>
          {time && (
             <div className="ml-1 flex justify-between">
              <p className="font-bold mr-1 text-xs text-slate-500">Saved </p>
              <p className="text-xs text-slate-500">{formatDateReadable(time)}</p>
            </div>
          )}
          <div className="ml-1  truncate text-xs text-slate-400">
            It has saved by {event.userId}
          </div>
          <div>
            results: todo
          </div>
        </>
      )
}
