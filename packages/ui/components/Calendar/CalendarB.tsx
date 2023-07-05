"use client";

import { useState } from "react";
import { CInput } from "./CalendarComponents";
import { CalendarCanvasB } from "./CalendarCanvas";
import moment from "moment"

interface CalendarBProps {
    earlyDate:Date, changeEarlyDate:(date:Date) => void,
    lastDate:Date, changeLastDate:(date:Date) => void
}

export const CalendarB = ({ earlyDate, changeEarlyDate, lastDate, changeLastDate }: CalendarBProps) => {

    const [startDt, setStartDT] = useState<Date | undefined>(earlyDate)
    const [endDt, setEndDT] = useState<Date | undefined>(lastDate)
    const [showCalendar, setShowCalendar] = useState(false)

    const setShowCalendarHandler = () => {
        setShowCalendar(prev => !prev)
    }
    const setStartDTHandler = (date?:Date) => {
        setStartDT(date)
    }
    const setEndDTHandler = (date?:Date) => {
        setEndDT(date)
    }
    const onApply = () => {
        console.log('apply')
        changeEarlyDate(startDt || moment().toDate())
        changeLastDate(endDt || moment().toDate())
        setShowCalendar(false)     
    }

    return (
        <div className="metricsCalendar">
            <div className="controls" >
                <CInput label={'TIME RANGE:'} >
                    <div className="inputWrapper" onClick={setShowCalendarHandler}>
                        {!startDt ? (
                            <div className="placeholder">Select date and time</div>
                        ) : (
                            <>{(!!startDt && !endDt) ? (
                                <div className="color-mild-light">
                                    <span className="text-white"> {moment(startDt).format(`MMM DD, HH:mm`)}</span> 
                                </div>
                            ) : (
                                <div className="color-mild-light"> Early Started:  
                                    <span className="text-white" style={{marginRight: '10px'}}> {moment(startDt).format(`MMM DD, HH:mm`)}</span> 
                                    Latest Started 
                                    <span className="text-white"> {moment(endDt).format(`MMM DD, HH:mm`)}</span> 
                                </div>
                            )} 
                            </>
                        )}
                        <span className="material-icons">expand_more</span>
                        <span className="material-icons-outlined">calendar_month</span>   
                    </div>
                    {showCalendar && <CalendarCanvasB earlyDate={earlyDate} lastDate={lastDate} setStartDT={setStartDTHandler} setEndDT={setEndDTHandler} onApply={onApply} />}     
                </CInput>
            </div>
        </div>
    )
}