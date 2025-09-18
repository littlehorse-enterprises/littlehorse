import { WfBuilderContainer } from '../components/WfBuilderContainer'
import { WorkflowProvider } from '../contexts/workflow/provider'
import { UIProvider } from '../contexts/ui/provider'

export default function WorkflowBuilderPage() {
  return (
    <>
      <main className="fixed inset-x-0 -my-4 h-[calc(100vh_-_80px)] w-screen bg-gray-700 text-gray-300">
        <WorkflowProvider>
          <UIProvider>
            <WfBuilderContainer />
          </UIProvider>
        </WorkflowProvider>
      </main>
    </>
  )
}
