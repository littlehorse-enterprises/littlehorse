import React, { FC } from 'react'
import { useFormContext } from 'react-hook-form'
import { Label } from '@/components/ui/label'

type Prop = { name: string; setIsDisabled: (checked: boolean) => void }
export const MarkFieldNull: FC<Prop> = ({ name, setIsDisabled }) => {
  const { setValue } = useFormContext()

  const handleCheckboxChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const checked = e.target.checked
    setIsDisabled(checked)
    if (checked) {
      setValue(name, null)
    } else {
      setValue(name, '')
    }
  }

  return (
    <Label htmlFor="mark-null" className="flex items-center gap-2">
      mark as null
      <input id="mark-null" type="checkbox" onChange={handleCheckboxChange} />
    </Label>
  )
}
