import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { forwardRef } from 'react'
import { useController, useFormContext } from 'react-hook-form'

interface SelectBoolProps {
  id: string
  className?: string
}

export const SelectBool = forwardRef<HTMLButtonElement, SelectBoolProps>(({ id, className }, ref) => {
  const { control } = useFormContext()
  const {
    field: { onChange, value, ...field },
  } = useController({
    name: id,
    control,
  })

  return (
    <Select onValueChange={onChange} value={value as string} {...field}>
      <SelectTrigger ref={ref} className={className}>
        <SelectValue placeholder="Select a value" />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="true">True</SelectItem>
        <SelectItem value="false">False</SelectItem>
      </SelectContent>
    </Select>
  )
})

SelectBool.displayName = 'SelectBool'
