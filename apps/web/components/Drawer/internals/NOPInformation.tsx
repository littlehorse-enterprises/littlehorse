import React, { useEffect, useState } from 'react'
import Image from 'next/image'
import splitArrowSvg from './split-arrow.svg'
import type { LH_EXCEPTION } from './FailureInformation'
import { FailureInformation } from './FailureInformation'
import {
  conditionSymbol,
  getFullVariableName,
  getNOP_RHS,
  parseKey
} from './drawerInternals'

interface NOPInformationProps {
	data: { sinkNodeName: string; condition: outgoingEdgesCondition }[]
	nodeName: string
	errorData: {
		handlerSpecName: string
		exception: LH_EXCEPTION | string
	}[]
	setToggleSideBar: (value: boolean, isError: boolean, code: string, language?: string) => void;
}

interface outgoingEdgesCondition {
	comparator: string
	left: {
		jsonPath: string
		variableName: string
	}
	right: {
		literalValue: any
	}
}

export function NOPInformation(props: NOPInformationProps) {

  const [ conditionsOnNode, setConditionsOnNode ] = useState<any>([])

  useEffect(() => {
    if (props.data !== undefined) {
      setConditionsOnNode(
        props.data.filter(edge => edge.condition !== undefined)
      )
    }
  }, [ props.data ])
  const onParseError = (data: any) => {
    if (typeof data  === 'string') {
      props.setToggleSideBar(true, true, data, 'str')
      return
    }
    const key = parseKey(data.type.toLowerCase())
    const error = data[key]
    props.setToggleSideBar(true, true, error, key)
  }
  return (
    <>
      {props.nodeName ? <>
        <div className='component-header'>
          <Image
            alt="split-arrow"
            height={24}
            src={splitArrowSvg}
            width={24}
          />
          <div>
            <p>NOP Information</p>
            <p className='component-header__subheader'>{props.nodeName}</p>
          </div>
        </div>
        <div className='drawer__nop__table'>
          <div className='drawer__nop__table__header '>Node Conditions</div>
          <div className='drawer__nop__table__header__subheaders'>
            <p className='center'>LHS</p>
            <p className='center'>Condition</p>
            <p className='center'>RHS</p>
            <p className='center'>SINK NODE NAME</p>
          </div>

          {props.data &&
							conditionsOnNode.length > 0 ?
            props.data.map(
              (
                element: {
										sinkNodeName: string
										condition: outgoingEdgesCondition
									},
                index: number
              ) => {
                return (
                  element.condition &&
										<div className='grid-4' key={index}>
										  <p className='center'>
										    {getFullVariableName(element.condition?.left)}
										  </p>
										  <p className='center'>
										    {conditionSymbol(element.condition?.comparator)}
										  </p>
										  <p className='center'>
										    {getNOP_RHS(element.condition?.right.literalValue)}
										  </p>
										  <p className='center'>{element.sinkNodeName}</p>
										</div>
                )
              }
            ) : <div className='grid-4 center'> The Node does not have conditions </div>}
        </div>
        <FailureInformation data={props.errorData} openError={onParseError} />
      </> : null}
    </>
  )
}
