export const getFullVariableName = (values: any) =>
	(values?.variableName || '') + (values?.jsonPath?.replace('$', '') || '')

export const getNOP_RHS = (value: any) => {
	if (value) {
		const variableType: string = value.type
		const correctKey = variableType.toLowerCase()

		return value[correctKey].toString()
	}

	return ''
}

export const conditionSymbol = (comparator: string) => {
	const conditions = {
		EQUALS: '=',
		NOT_EQUALS: '!=',
		GREATER_THAN: '>',
		GREATER_THAN_EQ: '>=',
		LESS_THAN: '<',
		LESS_THAN_EQ: '<='
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

		setDrawerData(content.data.result)
	} else console.warn('INVALID RESPONSE FROM API')
}
