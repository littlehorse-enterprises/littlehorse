import { useCallback, useEffect, useRef, useState } from 'react'

/*
 eslint-disable-next-line @typescript-eslint/no-empty-function
 */
export function NewScrollBar({ width=1000, windows=3, onChange=() => {} }:{
    width:number,
    windows:number,
    onChange?:(win:number) => void
}) {

    const scrollTrackRef = useRef<HTMLDivElement>(null)
    const scrollThumbRef = useRef<HTMLDivElement>(null)
    const [ thumbWidth, setThumbWidth ] = useState(0)
    const [ thumbLeftP, setThumbLeftP ] = useState(0)
    const [ initialThumbLeft, setInitialThumbLeft ] = useState(0)
    const [ win, setWin ] = useState(0)
    const [ scrollStartPosition, setScrollStartPosition ] = useState<number | null>(
        null
    )
    const [ isDragging, setIsDragging ] = useState(false)

    // eslint-disable-next-line react/hook-use-state -- seems is not used, analyze it further https://littlehorse.atlassian.net/browse/LH-236
    const [ _, setPercent ] = useState(0)

    useEffect( () => {
        const factor = thumbLeftP/(width-thumbWidth)
        const calculatedPercent = Math.ceil((thumbLeftP*100)/(width-thumbWidth))
        setPercent(calculatedPercent)
        const windowsRecalculateFactor = Math.floor(windows*(factor))+1
        setWin(windowsRecalculateFactor > windows ? windows : windowsRecalculateFactor)
    },[ thumbLeftP ])

    useEffect( () => {
        onChange(win || 1)
    },[ win ])
    useEffect( () => {
        let tW = width/windows
        tW = tW < 30 ? 30 : tW
        setThumbWidth(tW)
        const max = width-tW
        setThumbLeftP(max)
    },[ width,windows ])

    const handleTrackClick = useCallback(
        (e:any) => {
            e.preventDefault()
            e.stopPropagation()
            const { current: trackCurrent } = scrollTrackRef
            if (trackCurrent) {
                const { clientX } = e
                const rect = trackCurrent.getBoundingClientRect()
                const thumbOffset = -(thumbWidth / 2)
                const newPositionL = clientX-rect.x+thumbOffset
                setThumbLeftP(limit(newPositionL))
            }
        },
        [ thumbWidth ]
    )

    const limit = (value:number) => {

        const max = width-thumbWidth
        const val = value > max ? max :value
        return val < 0 ? 0 : val
    }

    const handleThumbMousedown = useCallback((e:any) => {
        e.preventDefault()
        e.stopPropagation()
        setScrollStartPosition(e.clientX)
        if (scrollThumbRef.current) {setInitialThumbLeft(parseInt(scrollThumbRef.current?.style.left || '0px', 10))}
        setIsDragging(true)
    }, [])

    const handleThumbMouseup = useCallback(
        (e:any) => {
            e.preventDefault()
            e.stopPropagation()
            if (isDragging) {
                setIsDragging(false)
            }
        },
        [ isDragging ]
    )
    const handleThumbMousemove = useCallback(
        (e:any) => {
            e.preventDefault()
            e.stopPropagation()
            if (isDragging) {
                if (scrollStartPosition){
                    const deltaY = (initialThumbLeft) + (e.clientX - scrollStartPosition)
                    setThumbLeftP(limit(deltaY))
                }
            }
        },
        [ isDragging, scrollStartPosition, thumbWidth ]
    )

    // Listen for mouse events to handle scrolling by dragging the thumb
    useEffect(() => {
        document.addEventListener('mousemove', handleThumbMousemove)
        document.addEventListener('mouseup', handleThumbMouseup)
        document.addEventListener('mouseleave', handleThumbMouseup)
        return () => {
            document.removeEventListener('mousemove', handleThumbMousemove)
            document.removeEventListener('mouseup', handleThumbMouseup)
            document.removeEventListener('mouseleave', handleThumbMouseup)
        }
    }, [ handleThumbMousemove, handleThumbMouseup ])

    return <div className="scrollBar">

        <div className="scrollBar__Canvas" style={{
            width:`${width}px`,
        }}>
            <div
                className="scrollBar__Track"
                onClick={handleTrackClick}
                ref={scrollTrackRef}
                style={{
                    cursor: isDragging ? 'grabbing' : 'pointer'
                }}
            />
            <div
                className="scrollBar__Thumb"
                onMouseDown={handleThumbMousedown}
                ref={scrollThumbRef}
                style={{
                    transition: isDragging ? 'none' : 'all .2s ease-out',
                    left:`${thumbLeftP}px`,
                    width: `${thumbWidth}px`,
                    cursor: isDragging ? 'grabbing' : 'grab',
                }}>
                {/* {percent}% window:{win} */}
            </div>
        </div>
    </div>
}
