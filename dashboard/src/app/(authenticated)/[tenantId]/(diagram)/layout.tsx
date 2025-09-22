'use client'
import { useState } from 'react'
import { ReactFlowProvider } from 'reactflow'
import { Modal, ModalProvider } from './context'

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  const [modal, setModal] = useState<Modal | null>(null)
  const [showModal, setShowModal] = useState(false)
  return (
    <ModalProvider value={{ modal, setModal, showModal, setShowModal }}>
      <ReactFlowProvider>{children}</ReactFlowProvider>
    </ModalProvider>
  )
}
