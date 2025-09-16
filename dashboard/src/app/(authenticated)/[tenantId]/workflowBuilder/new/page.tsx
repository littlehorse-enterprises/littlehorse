import { WfBuilderContainer } from '../components/wf-builder/WfBuilderContainer';
import { WorkflowProvider } from '../contexts/workflow/provider';
import { UIProvider } from '../contexts/ui/provider';

export default function WorkflowBuilderPage() {  
  return (
    <>
      <main className="bg-gray-700 text-gray-300 h-[calc(100vh_-_80px)] w-screen fixed inset-x-0 -my-4">
        <WorkflowProvider>
          <UIProvider>
            <WfBuilderContainer />
          </UIProvider>
        </WorkflowProvider>
      </main>
    </>
  );
}
