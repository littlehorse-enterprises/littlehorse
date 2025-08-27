'use client'
import { ReactFlowProvider } from 'reactflow'

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return <ReactFlowProvider>{children}</ReactFlowProvider>
}
