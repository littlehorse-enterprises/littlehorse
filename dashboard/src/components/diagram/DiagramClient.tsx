'use client'
import { NodeSelectionProvider } from '@/components/context/selection-context'
import LeftSidebar from '@/components/diagram/left-sidebar'
import RightSidebar from '@/components/diagram/right-sidebar'
import WorkflowDiagram from '@/components/diagram/workflow-diagram'
import { ResizablePanelGroup, ResizablePanel, ResizableHandle } from '@littlehorse-enterprises/ui-library/resizable'
import { WfSpec } from 'littlehorse-client/proto'
import { WfRunDetails } from '@/actions/getWfRunDetails'
import { CustomNode, CustomEdge } from '@/types'
import { useRef, useCallback } from 'react'
import { ImperativePanelHandle } from 'react-resizable-panels'

interface DiagramClientProps {
    wfSpec: WfSpec
    wfRunDetails?: WfRunDetails
    nodes: CustomNode[]
    edges: CustomEdge[]
}

const DEFAULT_SIZE = 20
const EXPANDED_SIZE = 40

export default function DiagramClient({ wfSpec, wfRunDetails, nodes, edges }: DiagramClientProps) {
    const leftPanelRef = useRef<ImperativePanelHandle>(null)
    const rightPanelRef = useRef<ImperativePanelHandle>(null)

    const handlePanelDoubleClick = useCallback((panelRef: React.RefObject<ImperativePanelHandle | null>) => {
        if (panelRef.current) {
            const currentSize = panelRef.current.getSize()
            const midpoint = (DEFAULT_SIZE + EXPANDED_SIZE) / 2
            const newSize = currentSize >= midpoint ? DEFAULT_SIZE : EXPANDED_SIZE
            panelRef.current.resize(newSize)
        }
    }, [])

    return (
        <NodeSelectionProvider>
            <ResizablePanelGroup direction="horizontal" className="h-full">
                <ResizablePanel ref={leftPanelRef} defaultSize={DEFAULT_SIZE} minSize={15} maxSize={EXPANDED_SIZE}>
                    <LeftSidebar wfSpec={wfSpec} wfRun={wfRunDetails?.wfRun} />
                </ResizablePanel>
                <ResizableHandle
                    withHandle
                    onDoubleClick={() => handlePanelDoubleClick(leftPanelRef)}
                />
                <ResizablePanel defaultSize={60} minSize={30}>
                    <WorkflowDiagram nodes={nodes} edges={edges} />
                </ResizablePanel>
                <ResizableHandle
                    withHandle
                    onDoubleClick={() => handlePanelDoubleClick(rightPanelRef)}
                />
                <ResizablePanel ref={rightPanelRef} defaultSize={DEFAULT_SIZE} minSize={15} maxSize={EXPANDED_SIZE}>
                    <RightSidebar wfSpec={wfSpec} wfRunDetails={wfRunDetails} />
                </ResizablePanel>
            </ResizablePanelGroup>
        </NodeSelectionProvider>
    )
}
