"use client";

import { useState, useRef } from "react";
import { CInput } from "./CalendarComponents";
import { CalendarCanvasB } from "./CalendarCanvas";
import { useOutsideClick } from "../../utils";
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
    // handler that will close the calendar when the user clicks outside of it
    const handleOutsideCalendarClick = () => {
        setShowCalendar(false)
    }
    const onApply = () => {
        console.log('apply')
        changeEarlyDate(startDt || moment().toDate())
        changeLastDate(endDt || moment().toDate())
        setShowCalendar(false)     
    }

    // ref used to locate the ancestor Ref so the handler doesn't reopen the calendar
    const ancestorOutsideCalendarClickRef = useRef<HTMLDivElement>(null);
    // anchor element that triggers handler when is not clicked.
    const outsideCalendarClickRef = useOutsideClick(handleOutsideCalendarClick, ancestorOutsideCalendarClickRef);

    return (
        <div className=" metricsCalendar">
            <div className="controls" >
                <CInput label={'TIME RANGE:'} >
                    <div className="inputWrapper" onClick={setShowCalendarHandler} ref={ancestorOutsideCalendarClickRef}>
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
                            ) } 
                            </>
                        )}
                        <span className="material-icons">expand_more</span>
                        <span className="material-icons-outlined">calendar_month</span>   
                    </div>
                    {showCalendar && <CalendarCanvasB earlyDate={earlyDate} lastDate={lastDate} setStartDT={setStartDTHandler} setEndDT={setEndDTHandler} onApply={onApply} outsideCalendarClickRef={outsideCalendarClickRef} />}     
                </CInput>
            </div>
        </div>
    )
}
