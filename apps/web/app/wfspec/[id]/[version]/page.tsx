import { WorkflowExecutionMetrics } from "./sections/WorkflowExecutionMetrics";
import { WfRunSearch } from "./sections/WfRunSearch";
import Breadcrumbs from "../../../../components/Breadcrumbs";
import { WfSpecVisualization } from "./sections/WfSpecVisualization";

const WfSpec = ({ params }: { params: { id: string; version: number } }) => {
    return (
        <>
            <h1>
                <span className="lighter">WfSpec</span>{" "}
                <span className="line">|</span>{" "}
                {params.id.charAt(0) + params.id.slice(1)}{" "}
            </h1>

            <Breadcrumbs
                pwd={[
                    {
                        title: "Cluster Overview",
                        href: "/",
                    },
                    {
                        title: `${params.id.charAt(0) + params.id.slice(1)}`,
                        active: true,
                    },
                ]}
            />

            <WfSpecVisualization id={params.id} version={params.version} />
            <WorkflowExecutionMetrics id={params.id} version={params.version} />

            <WfRunSearch id={params.id} version={params.version} />
        </>
    );
};

export default WfSpec;
