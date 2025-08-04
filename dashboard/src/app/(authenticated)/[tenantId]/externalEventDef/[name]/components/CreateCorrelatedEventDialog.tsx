'use client'
import { VARIABLE_TYPES } from '@/app/constants'
import { getTypedContent } from '@/app/utils/variables'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { ExternalEventDef } from 'littlehorse-client/proto'
import { Plus } from 'lucide-react'
import { useState } from 'react'
import { toast } from 'sonner'
import { getValidation } from '../../../(diagram)/components/Forms/components/validation'
import VariableInputField from '../../../(diagram)/components/Forms/components/VariableInputField'
import { putCorrelatedEvent } from '../actions/searchExternalEvent'

interface CreateCorrelatedEventDialogProps {
  externalEventDef: ExternalEventDef
  tenantId: string
  onSuccess: () => void
}

export default function CreateCorrelatedEventDialog({
  externalEventDef: spec,
  tenantId,
  onSuccess,
}: CreateCorrelatedEventDialogProps) {
  const [open, setOpen] = useState(false)
  const [isCreating, setIsCreating] = useState(false)
  const [key, setKey] = useState('')

  // Get the expected content type from ExternalEventDef typeInformation
  const expectedType = spec.typeInformation?.returnType?.type || 'STR'
  const [contentType, setContentType] = useState<string>(expectedType)
  const [contentValue, setContentValue] = useState('')
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
    if (!key.trim()) {
      toast.error('Correlation key is required')
      return
    }

    if (!contentValue.trim()) {
      toast.error('Content value is required')
      return
    }

    try {
      // Validate JSON if applicable
      if (contentType === 'JSON_OBJ' || contentType === 'JSON_ARR') {
        const validator = getValidation(contentType)
        if (validator) {
          const validationResult = validator(contentValue)
          if (validationResult !== true) {
            toast.error(validationResult)
            return
          }
        }
      }

      setIsCreating(true)

      await putCorrelatedEvent({
        tenantId,
        key,
        externalEventDefName: spec.id?.name ?? '',
        content: getTypedContent(contentType, contentValue),
      })

      toast.success('Correlated event created successfully')

      // Reset form and close dialog
      setKey('')
      setContentValue('')
      setContentType(expectedType) // Reset to the expected type for this ExternalEventDef
      setJsonError(null)
      setOpen(false)

      // Trigger refresh in parent
      onSuccess()
    } catch (error) {
      toast.error(String(error))
    } finally {
      setIsCreating(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="default" size="sm">
          <Plus className="mr-2 h-4 w-4" />
          Create
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Create Correlated Event</DialogTitle>
          <DialogDescription>Create a new correlated event for {spec.id?.name}</DialogDescription>
        </DialogHeader>

        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="key">Correlation Key</Label>
            <Input id="key" value={key} onChange={e => setKey(e.target.value)} placeholder="Enter correlation key" />
          </div>

          {!spec.typeInformation?.returnType?.type && (
            <div className="grid gap-2">
              <Label htmlFor="content-type">Content Type</Label>
              <Select
                value={contentType}
                onValueChange={value => {
                  setContentType(value)
                  setContentValue('') // Reset value when type changes
                  setJsonError(null)
                }}
              >
                <SelectTrigger id="content-type">
                  <SelectValue placeholder="Select content type" />
                </SelectTrigger>
                <SelectContent>
                  {Object.entries(VARIABLE_TYPES)
                    .filter(([type]) => type !== 'UNRECOGNIZED' && type !== 'WF_RUN_ID')
                    .map(([type, label]) => (
                      <SelectItem key={type} value={type}>
                        {label}
                      </SelectItem>
                    ))}
                </SelectContent>
              </Select>
            </div>
          )}

          {spec.typeInformation?.returnType?.type && (
            <div className="grid gap-2">
              <Label htmlFor="content-type">Content Type</Label>
              <div className="flex items-center px-1 py-2 text-sm font-medium text-foreground">
                <span className="inline-flex items-center rounded-md bg-blue-50 px-2 py-1 text-xs font-medium text-blue-700 ring-1 ring-inset ring-blue-700/10">
                  {VARIABLE_TYPES[contentType as keyof typeof VARIABLE_TYPES] || contentType}
                </span>
                <span className="ml-2 text-xs text-muted-foreground">(predefined by ExternalEventDef)</span>
              </div>
            </div>
          )}

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
          <Button type="button" variant="outline" onClick={() => setOpen(false)}>
            Cancel
          </Button>
          <Button
            type="button"
            onClick={handleSubmit}
            disabled={isCreating || !key.trim() || !contentValue.trim() || !!jsonError}
          >
            {isCreating ? 'Creating...' : 'Create Event'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
