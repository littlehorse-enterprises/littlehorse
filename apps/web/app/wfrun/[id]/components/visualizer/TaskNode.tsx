import { useEffect, useState } from "react";
import { nodename } from "../../../../../helpers/nodename";

export const TaskNode = ({d, run}:{d:any, run?:any}) => {
    // const [loops, setLoops] = useState([])


    //   useEffect( () => {
    //     console.log('dsadasd')
    //     if(d.loop) getLoops()
    //   },[])



    return <div className="viznode-canvas">
        <div className="ring">
            <div className={`node c${d.name} t${d.type} ${d.position<=run.currentNodePosition ? "" : "opacity50"}`} >
                <img src={`/${d.type}.svg`} alt={d.type} />
                {!!d.node.failureHandlers?.length && <img src={`/EXCEPTION.svg`} alt={d.type} />}
                
                <div>
                    {d.name.split('-').slice(1,-1).join('-')}
                </div>
                {(d.name.split('-')[0] == run.currentNodePosition) && run.status === 'ERROR' && <img src={`/EXCEPTION.svg`} alt={d.type} />}
                {(d.name.split('-')[0] == run.currentNodePosition) && <img src={`/distance.svg`} alt={d.type} />}
                {/* {loops ? 'y': 'n'} */}
                {d.loop && <img src={`/loop.svg`} alt={d.type} />}
            </div>
        </div>
        {/* {JSON.stringify(d)} */}
    </div>
} 