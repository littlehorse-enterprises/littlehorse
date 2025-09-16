import { WfBuilderContainer } from '../components/wf-builder/WfBuilderContainer';
import { WorkflowProvider } from '../contexts/workflow/provider';
import { UIProvider } from '../contexts/ui/provider';

export default function WorkflowBuilderPage() {  
  return (
    <>
      <main className="bg-gray-700 w-full grow text-gray-300 p-4">
        <WorkflowProvider>
          <UIProvider>
            <WfBuilderContainer />
          </UIProvider>
        </WorkflowProvider>
      </main>
    </>
  );
}
