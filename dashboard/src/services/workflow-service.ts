import { initialNodes, initialEdges } from "@/components/flow/mock-data"

// Mock data for workflow specs that would match the dashboard metadata
const workflowSpecs = [
  {
    id: "1",
    name: "example-basic",
    version: "v0.0",
    description: "A basic example workflow that demonstrates the core features of LittleHorse.",
    nodes: initialNodes,
    edges: initialEdges,
  },
  {
    id: "2",
    name: "it-request",
    version: "v0.0",
    description: "IT request approval workflow with manager approval steps.",
    nodes: initialNodes.map((node) => ({ ...node, id: `${node.id}-2` })),
    edges: initialEdges.map((edge) => ({
      ...edge,
      id: `${edge.id}-2`,
      source: `${edge.source}-2`,
      target: `${edge.target}-2`,
    })),
  },
  {
    id: "3",
    name: "onboarding-flow",
    version: "v1.1",
    description: "Employee onboarding process with multiple department approvals.",
    nodes: initialNodes.map((node) => ({ ...node, id: `${node.id}-3` })),
    edges: initialEdges.map((edge) => ({
      ...edge,
      id: `${edge.id}-3`,
      source: `${edge.source}-3`,
      target: `${edge.target}-3`,
    })),
  },
  {
    id: "4",
    name: "order-processing",
    version: "v2.3",
    description: "Order processing workflow from checkout to delivery.",
    nodes: initialNodes,
    edges: initialEdges,
  },
  {
    id: "5",
    name: "claim-processing",
    version: "v1.0",
    description: "Insurance claim processing workflow with fraud detection.",
    nodes: initialNodes.map((node) => ({ ...node, id: `${node.id}-5` })),
    edges: initialEdges.map((edge) => ({
      ...edge,
      id: `${edge.id}-5`,
      source: `${edge.source}-5`,
      target: `${edge.target}-5`,
    })),
  },
]

export function getWorkflowSpecById(id: string) {
  return workflowSpecs.find((spec) => spec.id === id) || null
}

export function getAllWorkflowSpecs() {
  return workflowSpecs
}
