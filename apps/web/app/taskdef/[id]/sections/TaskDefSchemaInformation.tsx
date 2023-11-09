'use client'
import { useEffect, useState } from 'react'
import { Loading } from 'ui/components/Loading'
import type { TaskDefInformationResponse } from '../../../../interfaces/TaskDefInformationResponse'
import type { InputVarsTaskDef } from '../../../../interfaces/InputVarsTaskDef'

export function TaskDefSchemaInformation({ id }: { id: string }) {
    const [ loadingInputVars, setLoadingInputVars ] = useState(true)
    const [ inputVars, setInputVars ] = useState<InputVarsTaskDef[]>([])

    const taskInformation = async () => {
        const res = await fetch('/api/information/taskDef', {
            method: 'POST',
            body: JSON.stringify({
                id
            }),
        })
        if (res.ok) {
            const data: TaskDefInformationResponse = await res.json()
            setInputVars(data.result.inputVars)
            setLoadingInputVars(false)
        }
    }
    useEffect(() => {
        taskInformation()
    }, [])

    const renderInputVars = () => {
        if (inputVars.length) {
            // eslint-disable-next-line react/no-array-index-key -- we are using the name + index
            return inputVars.map((row, index) => (<tr className="flex w-full" key={`${row.name}-${index}`}>
                <td className='w-full text-center'>{row.name}</td>
                <td className='w-full text-center'>{row.type}</td>
            </tr>))
        }

        if (loadingInputVars) {
            return <tr>
                <td className="flex flex-center justify-center" colSpan={2}>
                    <Loading />
                </td>
            </tr>
        }

        return <tr>
            <td className="flex flex-center justify-center" colSpan={2}>
                <div className="flex items-center justify-center">No variables required</div>
            </td>
        </tr>
    }

    return <section>
        <h2>TaskDef Schema Information</h2>

        <div className="table">
            <table className="flex-1" style={{ width: '100%' }}>
                <caption>Input Variables</caption>
                <thead className="flex" style={{
                    width: '100%'
                }}>
                    <tr className="flex w-full">
                        <th className="w-full text-center">NAME</th>
                        <th className="w-full text-center">TYPE</th>
                    </tr>
                </thead>
                <tbody className="no-scrollbar">
                    {renderInputVars()}
                </tbody>
            </table>
        </div>
    </section>
}