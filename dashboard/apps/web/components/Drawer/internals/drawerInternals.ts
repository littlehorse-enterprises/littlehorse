export const getFullVariableName = (values: any) =>
    (values?.variableName || '') + (values?.jsonPath?.replace('$', '') || '')

export const getNOPRHS = (value: any) => {
    if (value) {
        const variableType = Object.keys(value)[0]
        return value[variableType].toString();
    }

    return ''
}

export const conditionSymbol = (comparator: string) => {
    const conditions = {
        EQUALS:'=',
        NOT_EQUALS:'!=',
        GREATER_THAN: '>',
        GREATER_THAN_EQ: '>=',
        LESS_THAN_EQ: '<=',
        LESS_THAN: '<',
        IN: 'IN',
        NOT_IN: 'NOT IN'
    }

    return conditions[comparator as keyof typeof conditions]
}

export const nodeTypes = {
    TASK: 'task',
    NOP: 'nop_def',
    EXTERNAL_EVENT: 'externalEvent',
    START_THREAD: 'startThread',
    WAIT_FOR_THREAD: 'waitForThread'
}

export const getMainDrawerData = async (name: string, setDrawerData: any) => {
    const response = await fetch(`../../api/drawer/wfSpec/${name}`)

    if (response.ok) {
        const content = await response.json()

        setDrawerData(content)
    } else {console.error('INVALID RESPONSE FROM API')}
}

export const parseKey = (variableType: string) => {
    return variableType.split('_').map((w, i) => {
        if (i === 0) {return w.toLowerCase()}
        return w.charAt(0) + w.slice(1).toLowerCase()
    }).join('')
}

export const getThreadName = nodeName => {
    if (!nodeName) {return ''}
    const split = nodeName.split('-')
    return `${split[1]} ${split[2]}`
}
export const getThreadVarName = nodeName => {
    return nodeName.split('-').slice(1,-1).join('-')
}
