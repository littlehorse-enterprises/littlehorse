'use client'
import { VARIABLE_TYPES } from '@/app/constants'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'

interface VariableInputFieldProps {
    contentType: string
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
    jsonError
}: VariableInputFieldProps) {
    switch (contentType) {
        case 'BOOL':
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
        case 'JSON_OBJ':
        case 'JSON_ARR':
            return (
                <div>
                    <Textarea
                        value={contentValue}
                        onChange={e => {
                            const newValue = e.target.value
                            setContentValue(newValue)
                            validateJson?.(newValue, contentType)
                        }}
                        placeholder={`Enter ${VARIABLE_TYPES[contentType as keyof typeof VARIABLE_TYPES]?.toLowerCase()} value`}
                        className={`min-h-[120px] ${jsonError ? 'border-red-500' : contentValue.trim() ? 'border-green-500' : ''}`}
                    />
                    {jsonError && <div className="mt-1 text-xs text-red-500">{jsonError}</div>}
                    {!jsonError && contentValue.trim() && (
                        <div className="mt-1 text-xs text-green-500">
                            Valid JSON {contentType === 'JSON_OBJ' ? 'object' : 'array'}
                        </div>
                    )}
                </div>
            )
        case 'INT':
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
        case 'DOUBLE':
            return (
                <Input
                    type="number"
                    value={contentValue}
                    onChange={e => setContentValue(e.target.value)}
                    placeholder={`Enter ${VARIABLE_TYPES[contentType as keyof typeof VARIABLE_TYPES]?.toLowerCase()} value`}
                    step="0.01"
                />
            )
        case 'BYTES':
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
