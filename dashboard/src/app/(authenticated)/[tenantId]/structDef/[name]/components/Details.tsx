'use client'
import { FC } from 'react'
import { StructDef } from 'littlehorse-client/proto'

type DetailsProps = Pick<StructDef, 'id' | 'description'>

export const Details: FC<DetailsProps> = ({ id, description }) => {
  return (
    <div className="mb-4">
      <span className="italic">StructDef</span>
      <h1 className="block text-2xl font-bold">{id?.name}</h1>
      {description && <div className="italic">{description}</div>}
    </div>
  )
}
