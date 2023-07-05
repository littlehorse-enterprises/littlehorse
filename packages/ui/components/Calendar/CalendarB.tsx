"use client";

import moment from "moment"
import { useEffect, useState } from "react";
import { Content, CInput } from "./CalendarComponents";

export const CalendarCanvas = ({earlyDate, setEndDT, setStartDT, onApply, lastDate}:{
    earlyDate:Date,
    lastDate:Date,
    setEndDT:(dt?:Date) => void, 
    setStartDT:(dt?:Date) => void,
    onApply:() => void,
}) => {

    const [date, setDate] = useState<Date>(moment().toDate())
    const [selected, setSelected] = useState<Date | undefined>(earlyDate)
    const [endSelected, setEndSelected] = useState<Date | undefined>(lastDate)
    
    const updateEndSelectedH = (H:string) => {
        console.log('updateEndSelectedH')
        setEndSelected(endSelected ? moment(endSelected).set('hour',+H).toDate() : moment(selected).set('hour',+H).toDate() )
    }
    const updateEndSelectedM = (M:string) => {
        setEndSelected(endSelected ? moment(endSelected).set('minute',+M).toDate() : moment(selected).set('minute',+M).toDate() )
    }
    const updateSelectedH = (H:string) => {
        setSelected(moment(selected).set('hour',+H).toDate())
    }
    const updateSelectedM = (M:string) => {
        setSelected(moment(selected).set('minute',+M).toDate())
    }
    const selectDate = (date:Date) => {
        if(endSelected) {
            setSelected(date)
            setStartDT(date)
            setEndSelected(undefined)
            setEndDT(undefined)
        }else if(selected){
            if(date > selected){
                setEndDT(date)
                setEndSelected(date)
            }else{
                setEndDT(selected)
                setEndSelected(selected)
                setSelected(date)
                setStartDT(date)
            }
        }else{
            setSelected(date)
            setStartDT(date)
        }
       
    }
    useEffect(()=> {
        setEndDT(endSelected)
    },[endSelected])
    useEffect(()=> {
        setStartDT(selected)
    },[selected])
    const nextMonth = () => {
        setDate(date => moment(date).add(1,'month').toDate())
    }
    const prevMonth = () => {
        setDate(date => moment(date).subtract(1,'month').toDate())
    }

    return (
        <div className="flex float">
            <Content 
                init={date} 
                type={'MINUTES_5'} 
                nextMonth={nextMonth} 
                prevMonth={prevMonth} 
                selected={selected}
                selectDay={selectDate}
                endSelected={endSelected}
                updateSelectedH={updateSelectedH}
                updateEndSelectedM={updateEndSelectedM}
                updateEndSelectedH={updateEndSelectedH}
                updateSelectedM={updateSelectedM}
                onApply={onApply}
            />
        </div>
    )  
}
export const CalendarB = ({
    earlyDate, changeEarlyDate,
    lastDate, changeLastDate,
    }:{
        earlyDate:Date, changeEarlyDate:(date:Date) => void,
        lastDate:Date, changeLastDate:(date:Date) => void
    }) => {

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

    return <div className=" metricsCalendar">
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
                        ) } 
                        </>
                    )}
                    <span className="material-icons">expand_more</span>
                    <span className="material-icons-outlined">calendar_month</span>   
                </div>

                { showCalendar && <CalendarCanvas earlyDate={earlyDate} lastDate={lastDate} setStartDT={setStartDTHandler} setEndDT={setEndDTHandler} onApply={onApply} />}     
            </CInput>
        </div>

    

    </div>
}