import { VARIABLE_TYPE_ENTRIES } from '@/app/constants'
import { getTypedVariableValue } from '@/app/utils/variables'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { VariableValue } from 'littlehorse-client/proto'
import { useParams } from 'next/navigation'
import { useState } from 'react'
import { toast } from 'sonner'
import { getValidation } from '../../Forms/components/validation'
import VariableInputField from '../../Forms/components/VariableInputField'
import { NodeRunCase } from '../../Modals/NodeRun/AccordionContent'
import { putExternalEvent } from './actions'

export default function PostEvent({ nodeRun }: { nodeRun: NodeRunCase<'externalEvent'> }) {
  const [open, setOpen] = useState(false)
  const [contentType, setContentType] = useState<NonNullable<VariableValue['value']>['$case']>('str')
  const [contentValue, setContentValue] = useState<string>('')
  const [jsonError, setJsonError] = useState<string | null>(null)
  const tenantId = useParams().tenantId as string

  const validateJson = (value: string, type: string) => {
    if (!value.trim()) {
      setJsonError(null)
      return
    }

    try {
      const parsed = JSON.parse(value)

      if (type === 'JSON_OBJ' && Array.isArray(parsed)) {
        setJsonError('Expected a JSON object, but got an array')
      } else if (type === 'JSON_ARR' && !Array.isArray(parsed)) {
        setJsonError('Expected a JSON array, but got an object')
      } else {
        setJsonError(null)
      }
    } catch (error) {
      setJsonError((error as Error).message)
    }
  }

  const handleSubmit = async () => {
    const externalEventDefId = nodeRun.nodeType?.value?.externalEventDefId
    const wfRunId = nodeRun.id?.wfRunId

    if (!externalEventDefId || !wfRunId) return toast.error('No externalEventDefId or wfRunId')
    if (!contentValue.trim()) return toast.error('Content value is required')

    try {
      // Validate JSON if applicable
      if (contentType === 'jsonObj' || contentType === 'jsonArr') {
        const validator = getValidation(contentType)
        if (validator) {
          const validationResult = validator(contentValue)
          if (validationResult !== true) {
            return toast.error(validationResult)
          }
        }
      }

      await putExternalEvent({
        tenantId,
        externalEventDefId,
        wfRunId,
        content: getTypedVariableValue(contentType, contentValue),
      })
      toast.success('Event posted successfully')
      setOpen(false)
    } catch (error) {
      toast.error(String(error))
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="outline">Manually Post Event</Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Manually Post Event</DialogTitle>
        </DialogHeader>

        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="content-type">Content Type</Label>
            <Select
              value={contentType}
              onValueChange={(value: NonNullable<VariableValue['value']>['$case']) => {
                setContentType(value)
                setContentValue('') // Reset value when type changes
              }}
            >
              <SelectTrigger id="content-type">
                <SelectValue placeholder="Select content type" />
              </SelectTrigger>
              <SelectContent>
                {VARIABLE_TYPE_ENTRIES.map(([type, label]) => (
                  <SelectItem key={type} value={type}>
                    {label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="grid gap-2">
            <Label htmlFor="content-value">Content Value</Label>
            <VariableInputField
              contentType={contentType}
              contentValue={contentValue}
              setContentValue={setContentValue}
              validateJson={validateJson}
              jsonError={jsonError}
            />
          </div>
        </div>

        <DialogFooter>
          <DialogClose asChild>
            <Button variant="default">Cancel</Button>
          </DialogClose>
          <Button variant="outline" onClick={handleSubmit}>
            Post Event
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
