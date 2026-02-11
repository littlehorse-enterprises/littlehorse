'use client'

import { ReturnType } from 'littlehorse-client/proto'
import { FC } from 'react'
import { TypeDisplay } from './TypeDisplay'

type Props = {
  outputType?: ReturnType | null
}

export const OutputTypeDisplay: FC<Props> = ({ outputType }) => {
  if (!outputType) {
    return <span className="font-mono text-gray-400">Unknown Output Type</span>
  }

  if (!outputType.returnType) {
    return <span className="font-mono text-gray-400">void</span>
  }

  return <TypeDisplay definedType={outputType.returnType.definedType} />
}
