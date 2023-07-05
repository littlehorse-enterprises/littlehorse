"use client";

import * as React from "react";
import moment from "moment"
import { useEffect, useState } from "react";
import { Content, CInput } from "./CalendarComponents";
import { useOutsideClick } from "../../utils";

const wl = [
    {label:'1 day', value:"DAYS_1"},
    {label:'2 hours', value:"HOURS_2"},
    {label:'5 minutes', value:"MINUTES_5"},
]

// Calendar Canvas will be drilled with outsideCalendarClickRef so the click outside custom hook anchors the element and doesn't reopen
export const CalendarCanvas = ({type, setEndDT, setStartDT, onApply, lastDate, outsideCalendarClickRef}:{
    type:string,
    lastDate:Date,
    setEndDT:(dt?:Date) => void, 
    setStartDT:(dt?:Date) => void,
    onApply:() => void,
    outsideCalendarClickRef: any
}) => {

    const [date, setDate] = useState<Date>(moment().toDate())
    const [selected, setSelected] = useState<Date | undefined>(lastDate)
    const [endSelected, setEndSelected] = useState<Date | undefined>()
    
    const updateEndSelectedH = (H:string) => {
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
                type={type} 
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
const getFirstDate = (date:Date, type:string, windows:number) => {
    const dt = moment(date)
    if(type==='DAYS_1') return dt.subtract(windows,'days').toDate()
    if(type==='HOURS_2') return dt.subtract(windows*2,'hours').toDate()
    if(type==='MINUTES_5') return dt.subtract(windows*5,'minutes').toDate()
    return date
}
export const Calendar = ({
    type, changeType,
    lastDate, changeLastDate,
    noWindows, changeNoWindows
    }:{
        type:string, changeType:(type:string) => void,
        lastDate:Date, changeLastDate:(date:Date) => void,
        noWindows:number, changeNoWindows:(n:number) => void
    }) => {

    const [ttype, setType] = useState(wl.find( t => t.value ===type) || wl[1])
    const [startDt, setStartDT] = useState<Date | undefined>(getFirstDate(lastDate, type, noWindows))
    const [endDt, setEndDT] = useState<Date | undefined>(lastDate)
    const [showCalendar, setShowCalendar] = useState(false)
    const [showWL, setShowWL] = useState(false)

    const setTypeHandler = (e:any , type:any) => {
        e.stopPropagation()
        setType(type)
        changeType(type.value)
        setShowWL(false)
    }
    const setShowWLHandler = () => {
        setShowWL(prev => !prev)
        setShowCalendar(false)
    }
    const setShowCalendarHandler = () => {
        setShowCalendar(prev => !prev)
        setShowWL(false)
    }

    const setStartDTHandler = (date?:Date) => {
        setStartDT(date)
    }
    const setEndDTHandler = (date?:Date) => {
        setEndDT(date)
    }
    // handler that will close the dropdown menu when the user clicks on different part of the page.
    const handleOutsideClick = () => {
        setShowWL(false)
    };
    // handler that will close the calendar when the user clicks outside of it
    const handleOutsideCalendarClick = () => {
        setShowCalendar(false)
    }

    const onApply = () => {
        console.log('apply')
        const dt = moment(endDt || startDt || lastDate)
        // console.log(dt)
        // console.log(dt.valueOf())
        // console.log(dt.toDate())
        // console.log(dt.toLocaleString())
        // console.log(dt.format())
        // console.log(dt().format())
        let diff=0
        const endDte = endDt ? endDt : moment(startDt).endOf('day').toDate()

        if(type === 'DAYS_1'){
            diff = moment(endDte).diff(moment(startDt), 'days')
            diff= diff+1
        }else if(type === 'HOURS_2'){
            diff = moment(endDte).diff(moment(startDt), 'hours')
            diff = Math.ceil(diff/2)

        }else{
            diff = moment(endDte).diff(moment(startDt), 'minutes')
            diff = Math.ceil(diff/5)
        }
        changeNoWindows(diff>1500 ? 1500 : diff)
        changeLastDate( endDte || lastDate)
        setShowCalendar(false)
        
    }

    // ref used to locate the ancestor Ref so the handler doesn't reopen the dropdown
    const ancestorOutsideClickRef = React.useRef<HTMLDivElement>(null);
    // anchor element that triggers handler when is not clicked.
    const outsideClickRef = useOutsideClick(handleOutsideClick, ancestorOutsideClickRef);


    // ref used to locate the ancestor Ref so the handler doesn't reopen the calendar
    const ancestorOutsideCalendarClickRef = React.useRef<HTMLDivElement>(null);
    // anchor element that triggers handler when is not clicked.
    const outsideCalendarClickRef = useOutsideClick(handleOutsideCalendarClick, ancestorOutsideCalendarClickRef);

    return <div className=" metricsCalendar">
        <div className="controls" ref={ancestorOutsideClickRef}>
            <CInput label={'WINDOW LENGTH:'} onClick={setShowWLHandler} >
                {showWL ? (
                    <div className="placeholder">Select one</div>
                ) : (
                    <div className="text">{ttype.label}</div>
                )}
                <span className="material-icons">expand_more</span>
                { showWL && <div className="float" ref={outsideClickRef}>
                    {wl.map( w => <div key={w.value} className="option" onClick={e => setTypeHandler(e, w)}>{w.label}</div>)}
                </div>}
            </CInput>

            <CInput label={'TIME RANGE:'} >
                <div className="inputWrapper" onClick={setShowCalendarHandler} ref={ancestorOutsideCalendarClickRef}>
                    {!startDt ? (
                        <div className="placeholder">Select date and time</div>
                    ) : (
                        <>{(!!startDt && !endDt) ? (
                            <div className="color-mild-light">
                                <span className="text-white"> {moment(startDt).format(`MMM DD ${ttype.value === 'HOURS_2' ? 'HH:00' : '' } ${ttype.value === 'MINUTES_5' ? 'HH:mm' : '' } `)}</span> 
                            </div>
                        ) : (
                            <div className="color-mild-light"> from 
                                <span className="text-white"> {moment(startDt).format(`MMM DD ${ttype.value === 'HOURS_2' ? 'HH:00' : '' } ${ttype.value === 'MINUTES_5' ? 'HH:mm' : '' } `)}</span> 
                                to 
                                <span className="text-white"> {moment(endDt).format(`MMM DD ${ttype.value === 'HOURS_2' ? 'HH:00' : '' } ${ttype.value === 'MINUTES_5' ? 'HH:mm' : '' }`)}</span> 
                            </div>
                        ) } 
                        </>
                    )}
                    <span className="material-icons">expand_more</span>
                    <span className="material-icons-outlined">calendar_month</span>   
                </div>

                { showCalendar && <CalendarCanvas type={ttype.value} lastDate={lastDate} setStartDT={setStartDTHandler} setEndDT={setEndDTHandler} onApply={onApply} outsideCalendarClickRef={outsideCalendarClickRef}/>}     
            </CInput>
        </div>

    

    </div>
}