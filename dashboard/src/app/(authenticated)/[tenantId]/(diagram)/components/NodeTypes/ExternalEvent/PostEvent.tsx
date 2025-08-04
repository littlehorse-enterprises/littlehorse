import { VARIABLE_TYPES } from '@/app/constants'
import { getTypedContent } from '@/app/utils/variables'
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
import { NodeRun } from 'littlehorse-client/proto'
import { useState } from 'react'
import { toast } from 'sonner'
import { getValidation } from '../../Forms/components/validation'
import VariableInputField from '../../Forms/components/VariableInputField'
import { putExternalEvent } from './actions'

export default function PostEvent({ nodeRun }: { nodeRun: NodeRun }) {
  const [open, setOpen] = useState(false)
  const [contentType, setContentType] = useState<string>('STR')
  const [contentValue, setContentValue] = useState<string>('')
  const [jsonError, setJsonError] = useState<string | null>(null)

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
    const externalEventDefId = nodeRun.externalEvent?.externalEventDefId
    const wfRunId = nodeRun.id?.wfRunId

    if (!externalEventDefId || !wfRunId) return toast.error('No externalEventDefId or wfRunId')
    if (!contentValue.trim()) return toast.error('Content value is required')

    try {
      // Validate JSON if applicable
      if (contentType === 'JSON_OBJ' || contentType === 'JSON_ARR') {
        const validator = getValidation(contentType)
        if (validator) {
          const validationResult = validator(contentValue)
          if (validationResult !== true) {
            return toast.error(validationResult)
          }
        }
      }

      await putExternalEvent({
        externalEventDefId,
        wfRunId,
        content: getTypedContent(contentType, contentValue),
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
              onValueChange={value => {
                setContentType(value)
                setContentValue('') // Reset value when type changes
              }}
            >
              <SelectTrigger id="content-type">
                <SelectValue placeholder="Select content type" />
              </SelectTrigger>
              <SelectContent>
                {Object.entries(VARIABLE_TYPES)
                  .filter(([type]) => type !== 'UNRECOGNIZED')
                  .map(([type, label]) => (
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
