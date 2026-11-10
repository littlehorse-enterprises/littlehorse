import React, { useState } from 'react'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { TaskAttempt, VariableValue } from 'littlehorse-client/proto'
import { Modal, ModalContext } from '../../../../context/ModalContext'
import { OutputModal } from '../../../Modals/OutputModal'
import { Attempts } from '../Attempts'

jest.mock('../../../Modals', () => ({ Modals: () => null }))

const AttemptOutput = ({ value }: { value: VariableValue['value'] }) => {
  const [modal, setModal] = useState<Modal | null>(null)
  const [showModal, setShowModal] = useState(false)
  return (
    <ModalContext.Provider value={{ modal, setModal, showModal, setShowModal }}>
      <Attempts
        attempts={[TaskAttempt.create({ result: { oneofKind: 'output', output: { value } } })]}
        attemptIndex={0}
        setAttemptIndex={() => {}}
      />
      {modal && <OutputModal {...modal} />}
    </ModalContext.Provider>
  )
}

describe('expanded task results', () => {
  const writeText = jest.fn().mockResolvedValue(undefined)

  beforeEach(() => {
    writeText.mockClear()
    Object.defineProperty(navigator, 'clipboard', { configurable: true, value: { writeText } })
  })

  it.each<{ name: string; value: VariableValue['value']; preview: string; expanded: string }>([
    {
      name: 'JSON object',
      value: { oneofKind: 'jsonObj', jsonObj: '{"items":[{"ok":true}]}' },
      preview: '{"items":[{"ok":true}]}',
      expanded: '{\n    "items": [\n        {\n            "ok": true\n        }\n    ]\n}',
    },
    {
      name: 'JSON array',
      value: { oneofKind: 'jsonArr', jsonArr: '[{"ok":true}]' },
      preview: '[{"ok":true}]',
      expanded: '[\n    {\n        "ok": true\n    }\n]',
    },
    {
      name: 'JSON-looking string',
      value: { oneofKind: 'str', str: '{"ok":true}' },
      preview: '{"ok":true}',
      expanded: '{"ok":true}',
    },
    {
      name: 'struct',
      value: {
        oneofKind: 'struct',
        struct: { struct: { fields: { ok: { value: { value: { oneofKind: 'bool', bool: true } }, masked: false } } } },
      },
      preview: '{"ok":true}',
      expanded: '{"ok":true}',
    },
    {
      name: 'map',
      value: {
        oneofKind: 'map',
        map: {
          entries: [
            { key: { value: { oneofKind: 'str', str: 'ok' } }, value: { value: { oneofKind: 'bool', bool: true } } },
          ],
        },
      },
      preview: '{"ok":true}',
      expanded: '{"ok":true}',
    },
    {
      name: 'typed array',
      value: { oneofKind: 'array', array: { items: [{ value: { oneofKind: 'int', int: '1' } }] } },
      preview: '[1]',
      expanded: '[1]',
    },
  ])('displays and copies $name output while preserving its compact preview', async ({ value, preview, expanded }) => {
    const { container } = render(<AttemptOutput value={value} />)
    const previewElement = screen.getByText(preview)
    expect(previewElement.textContent).toBe(preview)

    fireEvent.mouseEnter(screen.getByText('result:').parentElement!)
    fireEvent.click(container.querySelector('.lucide-expand')!)

    const dialog = screen.getByRole('dialog')
    expect(dialog.querySelector('code')!.textContent).toBe(expanded)
    expect(previewElement.textContent).toBe(preview)

    fireEvent.click(dialog.querySelector('button')!)
    await waitFor(() => expect(writeText).toHaveBeenCalledWith(expanded))
  })
})
