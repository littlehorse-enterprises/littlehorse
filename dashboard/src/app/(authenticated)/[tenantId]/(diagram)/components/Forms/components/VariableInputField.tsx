'use client'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import { VariableValue } from 'littlehorse-client/proto'

interface VariableInputFieldProps {
  contentType: NonNullable<VariableValue['value']>['$case']
  contentValue: string
  setContentValue: (value: string) => void
  validateJson?: (value: string, type: string) => void
  jsonError?: string | null
}

export default function VariableInputField({
  contentType,
  contentValue,
  setContentValue,
  validateJson,
  jsonError,
}: VariableInputFieldProps) {
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
              validateJson?.(newValue, contentType)
            }}
            placeholder={`Enter ${contentType === 'jsonObj' ? 'json object' : 'json array'} value`}
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
          placeholder="Enter integer value"
          step="1"
        />
      )
    case 'double':
      return (
        <Input
          type="number"
          value={contentValue}
          onChange={e => setContentValue(e.target.value)}
          placeholder="Enter decimal value"
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
          placeholder="Enter string value"
        />
      )
  }
}
