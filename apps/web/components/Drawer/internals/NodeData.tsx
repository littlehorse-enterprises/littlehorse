import moment from 'moment'
import React from 'react'

export interface NodeDataProps {
    scheduled?: string;
    reachTime?: string;
    completionTime?: string;
    status?: string;
}

export function NodeData(props: NodeDataProps) {
    return (
        <div className="drawer__nodeData">
            <div className="drawer__nodeData__label">Node data</div>
            <div className="grid-3">
                {props.scheduled ? <p className="drawer__nodeData__header">SCHEDULED</p> : null}
                {props.scheduled ? <p className="drawer__nodeData__data">{props.scheduled ? moment(props.scheduled).format('MMMM DD, HH:mm:ss') : ''}</p> : null}
                <p className="drawer__nodeData__header">REACH TIME</p>
                <p className="drawer__nodeData__data">{props.reachTime ? moment(props.reachTime).format('MMMM DD, HH:mm:ss') : ''}</p>
                <p className="drawer__nodeData__header">COMPLETION TIME</p>
                <p className="drawer__nodeData__data">{props.completionTime ? moment(props.completionTime).format('MMMM DD, HH:mm:ss') : ''}</p>
                <p className="drawer__nodeData__header">STATUS</p>
                <p className="drawer__nodeData__data">{props.status}</p>
            </div>
        </div>
    )
}
