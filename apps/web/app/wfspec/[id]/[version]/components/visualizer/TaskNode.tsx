export const TaskNode = ({d}:{d:any}) => {

    return <div className="viznode-canvas">
        <div className="ring">
            <div className={`node c${d.name} t${d.type}`} >
                <img src={`/${d.type}.svg`} alt={d.type} />
                {!!d.node.failureHandlers?.length && <img src={`/EXCEPTION.svg`} alt={d.type} />}
                <div>
                    {d.name.split('-').slice(1,-1).join('-')}
                </div>
            </div>
        </div>
    </div>
} 