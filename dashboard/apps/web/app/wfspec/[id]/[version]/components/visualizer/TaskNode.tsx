export function TaskNode({ d }:{ d:any }) {

    return <div className="viznode-canvas">
        <div className="ring">
            <div className={`node c${d.name} t${d.type}`} id={`id${d.name}`} >
                <img alt={d.type} src={`/${d.type}.svg`} />
                {Boolean(d.node.failureHandlers?.length) && <img alt={d.type} src="/EXCEPTION.svg" />}
                <div>
                    {d.name.split('-').slice(1,-1).join('-')}
                </div>
            </div>
        </div>
    </div>
}
