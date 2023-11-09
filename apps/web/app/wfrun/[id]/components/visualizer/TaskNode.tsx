export function TaskNode({ d, run }:{ d:any, run?:any }) {
    // const [loops, setLoops] = useState([])


    //   useEffect( () => {
    //     console.log('dsadasd')
    //     if(d.loop) getLoops()
    //   },[])



    return <div className="viznode-canvas">
        <div className="ring">
            <div className={`node c${d.name} t${d.type} ${d.position<=run.currentNodePosition ? '' : 'opacity50'}`} >
                <img alt={d.type} src={`/${d.type}.svg`} />
                {Boolean(d.node?.failureHandlers?.length) && <img alt={d.type} src="/EXCEPTION.svg" />}
                
                <div>
                    {d.name?.split('-').slice(1,-1).join('-')}
                </div>
                {(d.name?.split('-')[0] === run.currentNodePosition) && run.status === 'ERROR' && <img alt={d.type} src="/EXCEPTION.svg" />}
                {(d.name?.split('-')[0] === run.currentNodePosition) && <img alt={d.type} src="/distance.svg" />}
                {/* {loops ? 'y': 'n'} */}
                {d.loop ? <img alt={d.type} src="/loop.svg" /> : null}
            </div>
        </div>
        {/* {JSON.stringify(d)} */}
    </div>
} 