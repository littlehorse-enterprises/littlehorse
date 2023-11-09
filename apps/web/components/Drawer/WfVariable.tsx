import React, { useEffect, useState } from 'react'
import { parseValueByType } from '../../helpers/parseValueByType'
import { VariableType } from '../../littlehorse-public-api/common_enums'
import type { VariableValue } from '../../littlehorse-public-api/variable'
import { parseKey } from './internals/drawerInternals'
import type { LHException } from './internals/FailureInformation'
import { FailureInformation } from './internals/FailureInformation'

interface MainDataItem {
    wfRunId?: string;
    threadRunNumber: string;
    name: string;
    index: number;
    run: any;
    type?: string;
}
interface WfVariableProps extends MainDataItem {
    setToggleSideBar: (
        value: boolean,
        isError: boolean,
        code: string,
        language?: string
    ) => void;
    errorData: {
        handlerSpecName: string;
        exception: LHException | string;
    }[];
}

function WfVariable(props: WfVariableProps) {
    const [ wfVariableValue, setWfVariableValue ] = useState<VariableValue | undefined>()
    const [ variableValueLabel, setVariableValueLabel ] = useState<
    string | number | boolean | undefined
    >()
    const [ longVariableValueClass, setLongVariableValueClass ] = useState('')

    const getVariableData = async (wfRunId: string, threadRunNumber: string, name: string) => {
        const res = await fetch('/api/drawer/variable', {
            method: 'POST',
            body: JSON.stringify({
                wfRunId,
                threadRunNumber,
                name,
            }),
        })

        if (res.ok) {
            const content = await res.json()
            const lhVariableValue: VariableValue = content.data.result.value
            setWfVariableValue(lhVariableValue)
        }
    }

    const onVariableValueClick = () => {
        if (wfVariableValue?.type === VariableType.JSON_OBJ) {
            props.setToggleSideBar(true, false, JSON.parse(wfVariableValue.jsonObj || '{}'), 'json')
        }

        if (wfVariableValue?.type === VariableType.JSON_ARR) {
            props.setToggleSideBar(true, false, JSON.parse(wfVariableValue.jsonArr || '[]'), 'json')
        }

        if (wfVariableValue?.type === VariableType.STR) {
            props.setToggleSideBar(true, false, wfVariableValue.str || '', 'json')
        }
    }

    const onParseError = (data: any) => {
        if (typeof data === 'string') {
            props.setToggleSideBar(true, true, data, 'str')
            return
        }
        const key = parseKey(data.type.toLowerCase())
        const error = data[key]
        props.setToggleSideBar(true, true, error, key)
    }

    const isAValueThatCouldBeReallyLong = (wfVariable: VariableValue | undefined) => {
        if (wfVariable === undefined) {
            return false
        }

        return  wfVariable.type === VariableType.JSON_OBJ || wfVariable.type === VariableType.JSON_ARR || wfVariable.type === VariableType.STR
    }

    useEffect(() => {
        getVariableData(props.wfRunId || '', props.threadRunNumber, props.name)
    
        setVariableValueLabel(parseValueByType(wfVariableValue))

        if (isAValueThatCouldBeReallyLong(wfVariableValue)) {
            setLongVariableValueClass('drawer__mainTable__clickable')
        } else {
            setLongVariableValueClass('') // Reset class if not JSON_OBJ
        }
    }, [ wfVariableValue ])

    return (
        <>
            <div className={`grid-2 ${props.run ? 'grid-3' : ''}`} key={props.index}>
                <p className="center">{props.name}</p>
                <p className="center">{props.type}</p>
                {props.run ? <p
                    className={`${
                        longVariableValueClass && (isAValueThatCouldBeReallyLong(wfVariableValue))  
                            ? 'json-text-collapsed drawer__mainTable__clickable'
                            : 'center'
                    } json-text-collapsed`}
                    onClick={onVariableValueClick}
                >
                    {variableValueLabel}
                </p> : null}
            </div>
            <FailureInformation data={props.errorData} openError={onParseError} />
        </>
    )
}
export default WfVariable
