import { copyToClipboard } from './copyToClipboard'

describe('copyToClipboard', () => {
  const originalClipboard = navigator.clipboard
  const execCommand = document.execCommand

  afterEach(() => {
    Object.defineProperty(navigator, 'clipboard', {
      configurable: true,
      value: originalClipboard,
    })
    document.execCommand = execCommand
    jest.restoreAllMocks()
  })

  it('uses the Clipboard API when available', async () => {
    const writeText = jest.fn().mockResolvedValue(undefined)
    Object.defineProperty(navigator, 'clipboard', {
      configurable: true,
      value: { writeText },
    })

    await copyToClipboard('hello')

    expect(writeText).toHaveBeenCalledWith('hello')
  })

  it('falls back to execCommand when the Clipboard API is unavailable', async () => {
    Object.defineProperty(navigator, 'clipboard', {
      configurable: true,
      value: undefined,
    })
    document.execCommand = jest.fn().mockReturnValue(true)

    await copyToClipboard('fallback text')

    expect(document.execCommand).toHaveBeenCalledWith('copy')
  })

  it('throws when both copy methods fail', async () => {
    Object.defineProperty(navigator, 'clipboard', {
      configurable: true,
      value: undefined,
    })
    document.execCommand = jest.fn().mockReturnValue(false)

    await expect(copyToClipboard('missing clipboard')).rejects.toThrow('Copy command failed')
  })
})
