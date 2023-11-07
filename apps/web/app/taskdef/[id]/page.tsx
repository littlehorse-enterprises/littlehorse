import Breadcrumbs from '../../../components/Breadcrumbs'
import { TaskDefSchemaInformation } from './sections/TaskDefSchemaInformation'
import { TaskExecutionMetrics } from './sections/TaskExecutionMetrics'
import { TaskRunSearch } from './sections/TaskRunSearch'

function WfRun({ params }: { params: { id: string } }) {
  return (
    <>
      <h1>
        <span className="lighter">TaskDef</span>{' '}
        <span className="line">|</span>{' '}
        {params.id.charAt(0) + params.id.slice(1)}{' '}
      </h1>

      <Breadcrumbs
        pwd={[
          {
            title: 'Cluster Overview',
            href: '/',
          },
          {
            title: `TaskDef: ${
              params.id.charAt(0) + params.id.slice(1)
            }`,
            active: true,
          },
        ]}
      />

      <TaskDefSchemaInformation id={params.id} />
      <TaskExecutionMetrics id={params.id} />
      <TaskRunSearch id={params.id} />
    </>
  )
}
export default WfRun
