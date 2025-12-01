import React from "react"
import { render, screen } from "@testing-library/react"
import { StartThreadNodeRun } from "../StartThreadNodeRun"

describe("StartThreadNodeRun", () => {
    it("renders Node Type and the Start Thread text", () => {
        const node = { childThreadId: "child-1", threadSpecName: "spec-A" } as any
        render(<StartThreadNodeRun node={node} />)

        expect(screen.getByText("Node Type:")).toBeInTheDocument()
        expect(screen.getByText("Start Thread")).toBeInTheDocument()
    })

    it("renders childThreadId and threadSpecName values", () => {
        const node = { childThreadId: "child-1", threadSpecName: "spec-A" } as any
        render(<StartThreadNodeRun node={node} />)

        expect(screen.getByText("childThreadId:")).toBeInTheDocument()
        expect(screen.getByText("child-1")).toBeInTheDocument()

        expect(screen.getByText("threadSpecName:")).toBeInTheDocument()
        expect(screen.getByText("spec-A")).toBeInTheDocument()
    })

    it("stringifies non-string values (e.g. numbers) for display", () => {
        const node = { childThreadId: 42, threadSpecName: 100 } as any
        render(<StartThreadNodeRun node={node} />)

        expect(screen.getByText("42")).toBeInTheDocument()
        expect(screen.getByText("100")).toBeInTheDocument()
    })
})
