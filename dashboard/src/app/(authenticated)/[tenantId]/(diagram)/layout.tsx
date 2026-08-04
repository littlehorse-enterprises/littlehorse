'use client'
import { useState } from 'react'
import { ReactFlowProvider } from 'reactflow'
import { Modal } from './context'
import { ModalProvider } from './context/ModalProvider'

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
