import React, { useEffect, useState } from 'react'
import { TaskDefInformation } from './internals/TaskDefInformation'
import { NOPInformation } from './internals/NOPInformation'
import { ExternalEventInformation } from './internals/ExternalEventInformation'
import { SpawnChildInformation } from './internals/SpawnChildInformation'
import { WaitChildInformation } from './internals/WaitChildInformation'
import { LH_EXCEPTION } from './internals/FailureInformation'

interface DrawerComponentProps {
	internalComponent?: string | undefined
	data: any
	nodeName: string
	wfRunDrawer: boolean
}

export const DrawerComponent = (props: DrawerComponentProps) => {
	const [rawData, setRawData] = useState<any>()
	const [selectedNodeData, setSelectedNodeData] = useState<any>()
	const [mainData, setMainData] = useState<{ name: string; type: string }[]>()
	const [selectedNode, setSelectedNode] = useState<any>()
	const [errorData, setErrorData] = useState<any>([])
	const [lastSelectedNode, setLastSelectedNode] = useState<any>()
	const [threadName, setThreadName] = useState<string>()

	const changeThread = () => {
		if (props.data.threadSpecs) {
			const keys = Object.keys(props.data.threadSpecs)

			setThreadName(keys[1])
		}
	}

	const getData: any = async (url: string, name: string) => {
		const response = await fetch(url + name)

		if (response.ok) {
			const content = await response.json()

			setRawData(content.data.result)
		} else console.warn('INVALID RESPONSE FROM API')
	}

	const getErrorData: any = (node: any) => {
		if (node) {
			const data = node.failureHandlers.map(
				(element: { handlerSpecName: string; specificFailure?: string }) => {
					let exception = 'ANY EXCEPTION'

					if (element.specificFailure)
						exception =
							LH_EXCEPTION[element.specificFailure as keyof typeof LH_EXCEPTION]

					if (exception === undefined && element.specificFailure)
						exception = element.specificFailure

					return {
						handlerSpecName: element.handlerSpecName,
						exception: exception
					}
				}
			)

			setErrorData(data)
		}
	}

	useEffect(() => {
		if (props.data) {
			if (mainData === undefined || threadName) {
				let selectedThread

				if (threadName === undefined) {
					selectedThread = props.data.entrypointThreadName
					setThreadName(selectedThread)
				}

				setMainData(
					props.data.threadSpecs[selectedThread || threadName].variableDefs
				)
			}

			if (props.nodeName !== lastSelectedNode) {
				setLastSelectedNode(props.nodeName)
				setSelectedNodeData(undefined)
				setSelectedNode(props.data.threadSpecs.entrypoint.nodes[props.nodeName])
				getErrorData(props.data.threadSpecs.entrypoint.nodes[props.nodeName])
			}

			const processComplexData = {
				task_def: () => {
					if (rawData === undefined)
						getData('../../api/drawer/taskDef/', selectedNode.task.taskDefName)

					if (rawData) {
						const processedData = rawData.inputVars.map(
							(
								element: { name: string; type: string; _: any },
								index: number
							) => {
								const currentVariable = selectedNode.task.variables[index]

								let variableName = currentVariable?.variableName

								const jsonPath = currentVariable?.jsonPath?.replace('$', '')

								if (jsonPath) variableName?.concat(jsonPath)

								return {
									name: element.name,
									type: element.type,
									variableName: variableName
								}
							}
						)

						setSelectedNodeData(processedData)
					}
				},
				external_def: () => {
					const processedData = selectedNode?.variableMutations.map(
						(element: {
							lhsName: string
							operation: string
							nodeOutput: any
						}) => {
							let literalValue = selectedNode.externalEvent.externalEventDefName

							if (element.nodeOutput.hasOwnProperty('jsonpath'))
								console.warn('Missing fix of property: jsonpath')

							if (element.nodeOutput.hasOwnProperty('jsonPath'))
								console.warn(
									'Property fixed: jsonPath; NEED TO SUBSTITUTE ON CODE'
								)

							//TODO: verify that the jsonPath property is right spelled
							const jsonPath = element.nodeOutput.jsonpath?.replace('$', '')

							if (jsonPath) literalValue = literalValue + jsonPath

							// FIXME: why is the concat not working?
							//literalValue.concat(jsonPath)

							return {
								mutatedVariable: element.lhsName,
								mutatedType: element.operation,
								literalValue: literalValue
							}
						}
					)

					setSelectedNodeData(processedData)
				},
				nop_def: () => {
					setSelectedNodeData(selectedNode?.outgoingEdges)
				}
			}

			if (props.internalComponent) {
				const complexData =
					processComplexData[
						props.internalComponent as keyof typeof processComplexData
					]

				if (complexData instanceof Function) complexData()
			}
		}
	}, [
		props.data,
		rawData,
		selectedNode,
		props.nodeName,
		props.internalComponent,
		threadName
	])

	return (
		<div className='flex flex-col gap-3 overflow-y-auto lh-round'>
			<>
				<div className='lh-border lh-round flex flex-col gap-2 p-3'>
					<p className='lh-entrypoint-header'>THREADSPEC</p>
					<div className='wfspec-select'>
						<select
							className='threadSpec'
							value={threadName}
							onChange={event => setThreadName(event.target.value)}
						>
							{props.data &&
								Object.keys(props.data.threadSpecs).map(name => {
									return <option value={name}>{name}</option>
								})}
						</select>
					</div>
					<p className='lh-entrypoint-header'>TYPE</p>
					{/* FIXME: wheres does type come from? */}
					<p>ENTRYPOINT</p>
				</div>
				<div className='lh-border lh-round'>
					<div className='p-3 text-center lh-drawer-title'>
						ThreadSpec Variables
					</div>
					<div className='grid grid-cols-2 lh-drawer-headers'>
						<p className='p-3 text-center'>NAME</p>
						<p className='p-3 text-center'>TYPE</p>
					</div>
					{mainData &&
						mainData.map(({ name, type }, index) => {
							return (
								<div key={index} className='grid grid-cols-2 items-center'>
									<p className='text-center p-3'>{name}</p>
									<p className='text-center p-3'>{type}</p>
								</div>
							)
						})}
				</div>
				{props.internalComponent === 'task_def' && (
					<TaskDefInformation
						{...{
							linkedThread: changeThread,
							data: selectedNodeData,
							nodeName: props.nodeName,
							errorData: errorData,
							wfRunDrawer: props.wfRunDrawer,
							wfRunData: {
								nodeData: {
									scheduled: 'May 24, 00:15:52.04',
									reachTime: 'May 05, 00:15:52.04',
									completionTime: 'May 05, 00:15:52.10',
									status: 'COMPLETED'
								},
								inputs: [
									{
										name: 'arg0',
										type: 'STR',
										value: 'Foo'
									}
								],
								outputs: [
									{
										type: 'STR',
										value: 'Hello, Foo'
									}
								]
							}
						}}
					/>
				)}
				{props.internalComponent === 'nop_def' && (
					<NOPInformation
						{...{
							data: selectedNodeData,
							nodeName: props.nodeName,
							errorData: errorData
						}}
					/>
				)}
				{props.internalComponent === 'external_def' && (
					<ExternalEventInformation
						{...{
							data: selectedNodeData,
							nodeName: props.nodeName,
							errorData: errorData,
							wfRunData: {
								nodeData: {
									scheduled: 'May 24, 00:15:52.04',
									reachTime: 'May 05, 00:15:52.04',
									completionTime: 'May 05, 00:15:52.10',
									status: 'COMPLETED'
								},
								guid: 'a07c56b5082f464eb0449f0db2516081',
								arrivedTime: 'May 05, 00:16:10.08',
								arrived: 'Yes',
								content: {
									type: 'STR',
									value: 'COLT'
								}
							},
							wfRunDrawer: props.wfRunDrawer
						}}
					/>
				)}
				{props.internalComponent === 'spawn_def' && (
					<SpawnChildInformation
						{...{
							linkedThread: changeThread,
							nodeName: props.nodeName,
							errorData: errorData,
							wfRunDrawer: props.wfRunDrawer
						}}
					/>
				)}
				{props.internalComponent === 'wait_def' && (
					<WaitChildInformation
						{...{
							linkedThread: changeThread,
							nodeName: props.nodeName,
							errorData: errorData,
							wfRunDrawer: props.wfRunDrawer
						}}
					/>
				)}
			</>
		</div>
	)
}
