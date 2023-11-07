import type { SleepNode } from '../../../../littlehorse-public-api/wf_spec'

const extract = (sleepNode: SleepNode): string => {
  const rawSecondsVariableName: string | undefined = sleepNode.rawSeconds?.variableName
  const timestampVariableName: string | undefined = sleepNode.timestamp?.variableName

  if (rawSecondsVariableName !== undefined) {
    return rawSecondsVariableName
  }

  if (timestampVariableName !== undefined) {
    return timestampVariableName
  }

  return `${sleepNode.rawSeconds?.literalValue?.int} Seconds`
}

const SleepLabelExtractor = {
  extract
}

export default SleepLabelExtractor
