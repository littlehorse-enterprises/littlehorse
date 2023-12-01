import type { SleepNode } from '../../../../littlehorse-public-api/wf_spec'
import { VariableType } from '../../../../littlehorse-public-api/common_enums'
import SleepLabelExtractor from './SleepLabelExtractor'

describe('extracts label out of sleep values', () => {
    /*
sleep:
- rawSeconds
= timestamp
- isoDate
for either of them it can be a variable or a literal value, if a literal value only ints will come
     */
    it('should return the number of seconds', () => {
        const expectedNumberOfSeconds = Math.floor(Math.random() * 10)
        const sleepNode: SleepNode = {
            'rawSeconds': {
                'literalValue': {
                    'type': VariableType.INT,
                    'int': expectedNumberOfSeconds
                }
            }
        }


        const label: string = SleepLabelExtractor.extract(sleepNode)
        expect(label).toEqual(`${expectedNumberOfSeconds} Seconds`)
    })

    it('should return the name of the variable in the label', () => {
        const sleepNode: SleepNode = {
            'rawSeconds': {
                'variableName': 'number-of-seconds-variable'
            }
        }


        const label: string = SleepLabelExtractor.extract(sleepNode)
        expect(label).toEqual(`number-of-seconds-variable`)
    })

    it('should return the timestamp variable as part of the label', () => {
        const sleepNode: SleepNode = {
            'timestamp': {
                'variableName': 'timestamp-to-wait-for'
            }
        }

        const label: string = SleepLabelExtractor.extract(sleepNode)
        expect(label).toEqual(`timestamp-to-wait-for`)
    })
})
