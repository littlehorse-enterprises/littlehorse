import * as React from "react";
import moment from "moment"

export interface YearSelectorProps{
    prevMonth:() => void
    nextMonth:() => void
    date:string
}
export const YearSelector = ({prevMonth, nextMonth, date}:YearSelectorProps) => {
    return <div className="header">
        <div onClick={prevMonth} className="lfBtn"><span className="material-icons">chevron_left</span> </div>
        <div className="yearSelector">
            <span>{moment.utc(date).format('MMMM Y')}</span>
        </div>
        <div onClick={nextMonth} className="lfBtn"><span className="material-icons">chevron_right</span></div>
    </div>
}
