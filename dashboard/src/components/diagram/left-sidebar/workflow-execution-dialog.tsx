'use client'
import {
    Dialog,
    DialogClose,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Button } from '@littlehorse-enterprises/ui-library/button'
import { ThreadVarDef, VariableType, WfSpec } from 'littlehorse-client/proto'
import { useParams, useRouter } from 'next/navigation'
import { useMemo, useState, useEffect } from 'react'
import { toast } from 'sonner'
import { getVariableDefType, VARIABLE_TYPES } from '@/utils/data/variables'
import { accessLevelLabels } from '@/utils/data/accessLevels'
import { executeRpc } from '@/actions/executeRPC'

export const DOT_REPLACEMENT_PATTERN = "*-/:DOT_REPLACE:"

interface WorkflowExecutionDialogProps {
    isOpen: boolean
    onClose: () => void
    wfSpec: WfSpec
}

interface FormValues {
    [key: string]: string | number | boolean | null
}

export default function WorkflowExecutionDialog({ isOpen, onClose, wfSpec }: WorkflowExecutionDialogProps) {
    const tenantId = useParams().tenantId as string
    const router = useRouter()

    // Initialize form state
    const [formValues, setFormValues] = useState<FormValues>({})
    const [errors, setErrors] = useState<{ [key: string]: string }>({})

    const wfSpecVariables = useMemo(() => {
        return wfSpec.threadSpecs?.entrypoint?.variableDefs?.map(variable => {
            const newVariable = { ...variable };
            if (newVariable.varDef?.name) {
                newVariable.varDef.name = newVariable.varDef.name.replace(/\./g, DOT_REPLACEMENT_PATTERN);
            }
            return newVariable;
        }) ?? [];
    }, [wfSpec]);

    // Initialize form values when dialog opens
    useEffect(() => {
        const initialValues: FormValues = { customWfRunId: '' };
        wfSpecVariables.forEach(variable => {
            if (variable.varDef?.name) {
                initialValues[variable.varDef.name] = variable.required ? '' : null;
            }
        });
        setFormValues(initialValues);
        setErrors({});
    }, [wfSpecVariables]);

    const validateField = (name: string, value: string | number | boolean | null, type: VariableType, required: boolean): string | null => {
        if (required && (!value || value === '')) {
            return `${name.replace(DOT_REPLACEMENT_PATTERN, '.')} is required`;
        }

        if (!value || value === '' || value === null) return null;

        switch (type) {
            case VariableType.INT:
                if (isNaN(Number(value))) return 'Must be a valid integer';
                break;
            case VariableType.DOUBLE:
                if (isNaN(Number(value))) return 'Must be a valid number';
                break;
            case VariableType.JSON_OBJ:
                try {
                    const parsed = JSON.parse(value as string);
                    if (typeof parsed !== 'object' || Array.isArray(parsed)) {
                        return 'Input must be a valid JSON object';
                    }
                } catch {
                    return 'Input must be valid JSON';
                }
                break;
            case VariableType.JSON_ARR:
                try {
                    const parsed = JSON.parse(value as string);
                    if (!Array.isArray(parsed)) {
                        return 'Input must be an array';
                    }
                } catch {
                    return 'Input must be valid JSON';
                }
                break;
        }
        return null;
    };

    const handleInputChange = (name: string, value: string | number | boolean | null, type: VariableType, required: boolean) => {
        const newValues = { ...formValues, [name]: value };
        setFormValues(newValues);

        // Clear field if it's optional and user wants to set it to null
        if (!required && value === '') {
            newValues[name] = null;
            setFormValues(newValues);
        }

        // Validate field
        const error = validateField(name, value, type, required);
        setErrors(prev => ({
            ...prev,
            [name]: error || ''
        }));
    };

    const formatVariablesPayload = (values: FormValues) => {
        return Object.keys(values).reduce((acc: Record<string, { str?: string; int?: number; double?: number; bool?: boolean; jsonObj?: string; jsonArr?: string; bytes?: Buffer }>, key) => {
            if (key === 'customWfRunId') return acc;
            const rawValue = values[key];
            if (rawValue === '' || rawValue === undefined || rawValue === null) return acc;

            const transformedKey = key.replace(DOT_REPLACEMENT_PATTERN, '.');
            const variable = wfSpecVariables.find(v => v.varDef?.name === key);
            if (!variable?.varDef) return acc;
            const type = getVariableDefType(variable.varDef);

            switch (type) {
                case VariableType.INT:
                    acc[transformedKey] = { int: Number(rawValue) };
                    break;
                case VariableType.DOUBLE:
                    acc[transformedKey] = { double: Number(rawValue) };
                    break;
                case VariableType.BOOL:
                    acc[transformedKey] = { bool: rawValue === 'true' || rawValue === true };
                    break;
                case VariableType.JSON_OBJ:
                    acc[transformedKey] = { jsonObj: JSON.stringify(JSON.parse(rawValue as string)) };
                    break;
                case VariableType.JSON_ARR:
                    acc[transformedKey] = { jsonArr: JSON.stringify(JSON.parse(rawValue as string)) };
                    break;
                case VariableType.BYTES:
                    acc[transformedKey] = { bytes: Buffer.from(rawValue as string, 'utf8') };
                    break;
                case VariableType.STR:
                default:
                    acc[transformedKey] = { str: String(rawValue) };
                    break;
            }
            return acc;
        }, {});
    };

    const handleSubmit = async () => {
        // Validate all fields
        const newErrors: { [key: string]: string } = {};
        let hasErrors = false;

        wfSpecVariables.forEach(variable => {
            if (variable.varDef?.name) {
                const type = getVariableDefType(variable.varDef);
                const error = validateField(
                    variable.varDef.name,
                    formValues[variable.varDef.name],
                    type,
                    variable.required
                );
                if (error) {
                    newErrors[variable.varDef.name] = error;
                    hasErrors = true;
                }
            }
        });

        if (hasErrors) {
            setErrors(newErrors);
            return;
        }

        const customWfRunId = formValues.customWfRunId as string;
        const variablesToSubmit = { ...formValues };
        delete variablesToSubmit.customWfRunId;

        if (!wfSpec.id) return;

        try {
            const wfRun = await executeRpc("runWf", {
                wfSpecName: wfSpec.id.name,
                majorVersion: wfSpec.id.majorVersion,
                revision: wfSpec.id.revision,
                id: customWfRunId || undefined,
                variables: formatVariablesPayload(variablesToSubmit),
            }, tenantId);

            if (!wfRun.id) return;
            toast.success('Workflow has been executed');
            onClose();
            router.push(`/${tenantId}/diagram/${wfSpec.id.name}/${wfSpec.id.majorVersion}.${wfSpec.id.revision}?wfRunId=${wfRun.id.id}`);
        } catch (error: unknown) {
            if (error instanceof Error) {
                toast.error(error.message)
            } else {
                toast.error("An error occurred")
            }
        }
    };

    const renderField = (variable: ThreadVarDef) => {
        if (!variable.varDef?.name) return null;

        const { name } = variable.varDef;
        const type = getVariableDefType(variable.varDef);
        const value = formValues[name];
        const error = errors[name];
        const displayName = name.replace(DOT_REPLACEMENT_PATTERN, '.');

        return (
            <div key={name} className="space-y-2">
                <div className="flex justify-between items-center">
                    <Label htmlFor={name} className="flex items-center gap-2">
                        {displayName}
                        <span className="rounded bg-green-300 p-1 text-xs">
                            {accessLevelLabels[variable.accessLevel]}
                        </span>
                        {variable.required ? (
                            <span className="rounded bg-red-300 p-1 text-xs">Required</span>
                        ) : (
                            <span className="rounded bg-gray-300 p-1 text-xs">Optional</span>
                        )}
                    </Label>
                </div>

                {type === VariableType.BOOL ? (
                    <Select
                        value={value?.toString() || ''}
                        onValueChange={(val) => handleInputChange(name, val, type, variable.required)}
                    >
                        <SelectTrigger className={error ? 'border-destructive' : ''}>
                            <SelectValue placeholder="Select True or False" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="true">True</SelectItem>
                            <SelectItem value="false">False</SelectItem>
                        </SelectContent>
                    </Select>
                ) : type === VariableType.JSON_OBJ || type === VariableType.JSON_ARR ? (
                    <Textarea
                        value={value as string || ''}
                        onChange={(e) => handleInputChange(name, e.target.value, type, variable.required)}
                        placeholder={`Enter ${VARIABLE_TYPES[type]?.toLowerCase()} value`}
                        className={error ? 'border-destructive' : ''}
                    />
                ) : (
                    <Input
                        type={type === VariableType.INT || type === VariableType.DOUBLE ? 'number' : 'text'}
                        value={value as string || ''}
                        onChange={(e) => handleInputChange(name, e.target.value, type, variable.required)}
                        step={type === VariableType.DOUBLE ? '0.01' : undefined}
                        placeholder={`Enter ${VARIABLE_TYPES[type]?.toLowerCase()} value`}
                        className={error ? 'border-destructive' : ''}
                    />
                )}

                {error && (
                    <p className="text-sm text-destructive">{error}</p>
                )}
            </div>
        );
    };

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="max-w-md">
                <DialogHeader>
                    <DialogTitle>Execute {wfSpec.id?.name}</DialogTitle>
                    <DialogDescription className="text-primary/50">
                        You can leave all optional fields blank if desired.
                    </DialogDescription>
                </DialogHeader>

                <div className="space-y-4">
                    {/* Custom WfRun ID field */}
                    <div className="space-y-2">
                        <Label htmlFor="customWfRunId" className="flex items-center gap-2">
                            Custom WfRun Id
                            <span className="rounded bg-gray-300 p-1 text-xs">Optional</span>
                        </Label>
                        <Input
                            type="text"
                            id="customWfRunId"
                            value={formValues.customWfRunId as string || ''}
                            onChange={(e) => setFormValues(prev => ({ ...prev, customWfRunId: e.target.value }))}
                            placeholder="Enter string value"
                        />
                    </div>

                    {/* Workflow variables */}
                    {wfSpecVariables.map(renderField)}
                </div>

                <DialogFooter>
                    <DialogClose className="mr-4">Cancel</DialogClose>
                    <Button onClick={handleSubmit} type="submit">
                        Execute Workflow
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}