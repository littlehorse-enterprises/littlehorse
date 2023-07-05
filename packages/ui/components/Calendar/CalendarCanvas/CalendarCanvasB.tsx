"use client"

import React, { useState, useEffect } from "react"
import { Content } from "../CalendarComponents"
import moment from "moment"

interface CalendarCanvasBProps {
    earlyDate:Date,
    lastDate:Date,
    setEndDT:(dt?:Date) => void, 
    setStartDT:(dt?:Date) => void,
    onApply:() => void,
    outsideCalendarClickRef: React.LegacyRef<HTMLDivElement>
}

export const CalendarCanvasB = ({earlyDate, setEndDT, setStartDT, onApply, lastDate, outsideCalendarClickRef}: CalendarCanvasBProps) => {

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
        <div className="flex float" ref={outsideCalendarClickRef}>
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