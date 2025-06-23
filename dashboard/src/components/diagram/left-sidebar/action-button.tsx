'use client'
import { Button } from '@littlehorse-enterprises/ui-library/button'
import { Play, Square, LifeBuoy, RotateCcw } from 'lucide-react'
import { cn } from '@/utils/ui/utils'
import { useState } from 'react'
import ActionConfirmationDialog from './action-confirmation-dialog'
import WorkflowExecutionDialog from './workflow-execution-dialog'
import { WfSpec, WfRun } from 'littlehorse-client/proto'
import { executeRpc } from '@/actions/executeRPC'
import { useParams } from 'next/navigation'

interface ActionButtonProps {
    variant: 'run' | 'stop' | 'rescue' | 'resume'
    wfSpec?: WfSpec // Only required for run
    wfRun?: WfRun // Required for stop, rescue, resume
}

export default function ActionButton({ variant, wfSpec, wfRun }: ActionButtonProps) {
    const [showConfirmation, setShowConfirmation] = useState(false)
    const [showRunDialog, setShowRunDialog] = useState(false)
    const { tenantId } = useParams<{ tenantId: string }>()

    const variantConfig = {
        run: {
            icon: Play,
            label: 'Run Workflow',
            styles: "bg-emerald-600 text-white hover:bg-emerald-700 hover:shadow-emerald-200/50",
        },
        stop: {
            icon: Square,
            label: 'Stop Workflow',
            styles: "bg-red-600 text-white hover:bg-red-700 hover:shadow-red-200/50",
            confirmation: {
                title: 'Stop Workflow',
                description: 'Are you sure you want to stop this workflow? This will halt the current execution.',
                confirmText: 'Stop Workflow',
                variant: 'destructive' as const
            }
        },
        rescue: {
            icon: LifeBuoy,
            label: 'Rescue Workflow',
            styles: "bg-blue-600 text-white hover:bg-blue-700 hover:shadow-blue-200/50",
            confirmation: {
                title: 'Rescue Workflow',
                description: 'Are you sure you want to rescue this workflow? This will attempt to recover from the error state.',
                confirmText: 'Rescue Workflow',
                variant: 'default' as const
            }
        },
        resume: {
            icon: RotateCcw,
            label: 'Resume Workflow',
            styles: "bg-green-600 text-white hover:bg-green-700 hover:shadow-green-200/50",
            confirmation: {
                title: 'Resume Workflow',
                description: 'Are you sure you want to resume this workflow? This will continue the halted execution.',
                confirmText: 'Resume Workflow',
                variant: 'default' as const
            }
        }
    }

    const config = variantConfig[variant]
    const Icon = config.icon

    const handleClick = () => {
        if (variant === 'run') {
            setShowRunDialog(true)
        } else {
            setShowConfirmation(true)
        }
    }

    const handleConfirm = () => {
        if (!tenantId) return

        switch (variant) {
            case 'stop':
                if (wfRun?.id) {
                    executeRpc("stopWfRun", {
                        wfRunId: wfRun.id,
                        threadRunNumber: 0
                    }, tenantId)
                }
                break
            case 'rescue':
                if (wfRun?.id) {
                    executeRpc("rescueThreadRun", {
                        wfRunId: wfRun.id,
                        threadRunNumber: 0,
                        skipCurrentNode: false,
                    }, tenantId)
                }
                break
            case 'resume':
                if (wfRun?.id) {
                    executeRpc("resumeWfRun", {
                        wfRunId: wfRun.id,
                        threadRunNumber: 0,
                    }, tenantId)
                }
                break
        }
    }

    type ConfirmationConfig = {
        confirmation: {
            title: string;
            description: string;
            confirmText: string;
            variant: 'default' | 'destructive';
        }
    };
    const hasConfirmation = (c: typeof config): c is typeof config & ConfirmationConfig => {
        return 'confirmation' in c;
    };

    return (
        <>
            <div className="mt-auto border-t border-border p-4">
                <Button
                    onClick={handleClick}
                    className={cn(
                        "w-full transition-all duration-200 ease-in-out transform hover:scale-[1.02] font-medium shadow-sm",
                        config.styles
                    )}
                >
                    <Icon className="mr-2 h-4 w-4 shrink-0" />
                    {config.label}
                </Button>
            </div>

            {variant === 'run' && wfSpec && (
                <WorkflowExecutionDialog
                    isOpen={showRunDialog}
                    onClose={() => setShowRunDialog(false)}
                    wfSpec={wfSpec}
                />
            )}
            {variant !== 'run' && hasConfirmation(config) && (
                <ActionConfirmationDialog
                    isOpen={showConfirmation}
                    onClose={() => setShowConfirmation(false)}
                    onConfirm={handleConfirm}
                    title={config.confirmation.title}
                    description={config.confirmation.description}
                    confirmText={config.confirmation.confirmText}
                    variant={config.confirmation.variant}
                />
            )}
        </>
    )
}