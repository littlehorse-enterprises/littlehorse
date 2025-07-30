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
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { VariableValue } from 'littlehorse-client/proto'
import { useState } from 'react'
import { toast } from 'sonner'
import { getValidation } from '../../Forms/components/validation'
import { NodeRunCase } from '../../Modals/NodeRun/AccordionContent'
import { putExternalEvent } from './actions'

export default function PostEvent({ nodeRun }: { nodeRun: NodeRunCase<'externalEvent'> }) {
  const [open, setOpen] = useState(false)
  const [contentType, setContentType] = useState<NonNullable<VariableValue['value']>['$case']>('str')
  const [contentValue, setContentValue] = useState<string>('')
  const [jsonError, setJsonError] = useState<string | null>(null)
  const { tenantId } = useWhoAmI()

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
    const externalEventDefId = nodeRun.nodeType.value.externalEventDefId
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
        content: getTypedContent(contentType, contentValue),
      })
      toast.success('Event posted successfully')
      setOpen(false)
    } catch (error) {
      toast.error(String(error))
    }
  }

  const renderInputField = () => {
    switch (contentType) {
      case 'bool':
        return (
          <Select value={contentValue} onValueChange={setContentValue}>
            <SelectTrigger>
              <SelectValue placeholder="Select a value" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="true">True</SelectItem>
              <SelectItem value="false">False</SelectItem>
            </SelectContent>
          </Select>
        )
      case 'jsonObj':
      case 'jsonArr':
        return (
          <div>
            <Textarea
              value={contentValue}
              onChange={e => {
                const newValue = e.target.value
                setContentValue(newValue)
                validateJson(newValue, contentType)
              }}
              placeholder={`Enter ${VARIABLE_TYPES[contentType as keyof typeof VARIABLE_TYPES]?.toLowerCase()} value`}
              className={`min-h-[120px] ${jsonError ? 'border-red-500' : contentValue.trim() ? 'border-green-500' : ''}`}
            />
            {jsonError && <div className="mt-1 text-xs text-red-500">{jsonError}</div>}
            {!jsonError && contentValue.trim() && (
              <div className="mt-1 text-xs text-green-500">
                Valid JSON {contentType === 'jsonObj' ? 'object' : 'array'}
              </div>
            )}
          </div>
        )
      case 'int':
        return (
          <Input
            type="number"
            inputMode="numeric"
            pattern="[0-9]*"
            onKeyDown={e => {
              if (e.key === '.' || e.key === ',') {
                e.preventDefault()
              }
            }}
            value={contentValue}
            onChange={e => setContentValue(e.target.value)}
            placeholder={`Enter ${VARIABLE_TYPES[contentType as keyof typeof VARIABLE_TYPES]?.toLowerCase()} value`}
            step="1"
          />
        )
      case 'double':
        return (
          <Input
            type="number"
            value={contentValue}
            onChange={e => setContentValue(e.target.value)}
            placeholder={`Enter ${VARIABLE_TYPES[contentType as keyof typeof VARIABLE_TYPES]?.toLowerCase()} value`}
            step="0.01"
          />
        )
      case 'bytes':
        return (
          <div>
            <Input
              type="text"
              value={contentValue}
              onChange={e => setContentValue(e.target.value)}
              placeholder="Enter data to be converted to bytes (UTF-8 encoded)"
            />
            <p className="mt-1 text-xs text-gray-500">
              Input will be converted to bytes using UTF-8 encoding. Use plain text for standard strings.
            </p>
          </div>
        )
      default:
        return (
          <Input
            type="text"
            value={contentValue}
            onChange={e => setContentValue(e.target.value)}
            placeholder={`Enter ${VARIABLE_TYPES[contentType as keyof typeof VARIABLE_TYPES]?.toLowerCase() || 'string'} value`}
          />
        )
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
              onValueChange={(value: keyof typeof VARIABLE_TYPES) => {
                setContentType(value)
                setContentValue('') // Reset value when type changes
              }}
            >
              <SelectTrigger id="content-type">
                <SelectValue placeholder="Select content type" />
              </SelectTrigger>
              <SelectContent>
                {Object.entries(VARIABLE_TYPES).map(([type, label]) => (
                  <SelectItem key={type} value={type}>
                    {label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="grid gap-2">
            <Label htmlFor="content-value">Content Value</Label>
            {renderInputField()}
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
