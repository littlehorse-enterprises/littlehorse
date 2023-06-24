import { useCallback, useEffect, useRef, useState } from "react";

export const NewScrollBar = ({width=1000, windows=3, onChange=() => {}}:{
    width:number,
    windows:number,
    onChange?:(win:number) => void
}) => {

    const scrollTrackRef = useRef<HTMLDivElement>(null);
    const scrollThumbRef = useRef<HTMLDivElement>(null);
    const [thumbWidth, setThumbWidth] = useState(0);
    const [thumbLeftP, setThumbLeftP] = useState(0);
    const [initialThumbLeft, setInitialThumbLeft] = useState(0);
    const [win, setWin] = useState(0);
    const [scrollStartPosition, setScrollStartPosition] = useState<number | null>(
        null
    );
    const [isDragging, setIsDragging] = useState(false);
    const [percent, setPercent] = useState(0);
    
    useEffect( () => {
        let factor = thumbLeftP/(width-thumbWidth)
        let percent = Math.ceil((thumbLeftP*100)/(width-thumbWidth))
        setPercent(percent)
        let win = Math.floor(windows*(factor))+1
        setWin(win > windows ? windows : win)
    },[thumbLeftP])

    useEffect( () => {
        onChange(win || 1)
    },[win])
    useEffect( () => {
        let tW = width/windows
        tW = tW < 30 ? 30 : tW
        setThumbWidth(tW)
        let max = width-tW
        setThumbLeftP(max)
    },[width,windows])

    const handleTrackClick = useCallback(
    (e:any) => {
        e.preventDefault();
        e.stopPropagation();
        const { current: trackCurrent } = scrollTrackRef;
        if (trackCurrent) {
        const { clientX } = e;
        const rect = trackCurrent.getBoundingClientRect();
        const thumbOffset = -(thumbWidth / 2);
        const newPositionL = clientX-rect.x+thumbOffset
        setThumbLeftP(limit(newPositionL))
        }
    },
    [thumbWidth]
    );

    const limit = (value:number) => {

        let max = width-thumbWidth
        let val = value > max ? max :value
        return val < 0 ? 0 : val
    }

    const handleThumbMousedown = useCallback((e:any) => {
        e.preventDefault();
        e.stopPropagation();
        setScrollStartPosition(e.clientX);
        if (scrollThumbRef.current) setInitialThumbLeft(parseInt(scrollThumbRef.current?.style.left || '0px', 10));
        setIsDragging(true);
      }, []);
    
      const handleThumbMouseup = useCallback(
        (e:any) => {
          e.preventDefault();
          e.stopPropagation();
          if (isDragging) {
            setIsDragging(false);
          }
        },
        [isDragging]
      );
      const handleThumbMousemove = useCallback(
        (e:any) => {
          e.preventDefault();
          e.stopPropagation();
          if (isDragging) {
            if(scrollStartPosition){
                const deltaY = (initialThumbLeft) + (e.clientX - scrollStartPosition);
                setThumbLeftP(limit(deltaY))
            }
          }
        },
        [isDragging, scrollStartPosition, thumbWidth]
      );

    // Listen for mouse events to handle scrolling by dragging the thumb
    useEffect(() => {
        document.addEventListener('mousemove', handleThumbMousemove);
        document.addEventListener('mouseup', handleThumbMouseup);
        document.addEventListener('mouseleave', handleThumbMouseup);
        return () => {
        document.removeEventListener('mousemove', handleThumbMousemove);
        document.removeEventListener('mouseup', handleThumbMouseup);
        document.removeEventListener('mouseleave', handleThumbMouseup);
        };
    }, [handleThumbMousemove, handleThumbMouseup]);

    return <div className="scrollBar">
    
    <div className="scrollBar__Canvas" style={{
        width:`${width}px`,
    }}>
        <div
            className="scrollBar__Track"
            ref={scrollTrackRef}
            onClick={handleTrackClick}
            style={{ 
                cursor: isDragging ? 'grabbing' : 'pointer'
             }}
          ></div>
        <div 
        ref={scrollThumbRef}
        onMouseDown={handleThumbMousedown}
        className="scrollBar__Thumb"
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