import { describe, expect, it, jest } from '@jest/globals'
import { z } from 'zod'
import { VariableType } from '../proto/common_enums'
import { LHConfig } from '../LHConfig'
import { createTaskWorker, defineTask } from './lhTaskWorker'

describe('createTaskWorker', () => {
  it('registers task return type from outputSchema', async () => {
    const putTaskDef = jest.fn(async () => ({ id: { name: 'greet' } }))
    const config = {
      getClient: () => ({ putTaskDef }),
    } as unknown as LHConfig

    async function greet(name: string): Promise<string> {
      return `Hello, ${name}!`
    }

    const task = defineTask(greet, {
      inputVars: { name: z.string() },
      outputSchema: z.string(),
    })

    await createTaskWorker(task, 'greet', config).registerTaskDef()

    expect(putTaskDef).toHaveBeenCalledWith({
      name: 'greet',
      inputVars: [
        {
          name: 'name',
          typeDef: {
            definedType: { $case: 'primitiveType', value: VariableType.STR },
            masked: false,
          },
        },
      ],
      returnType: {
        returnType: {
          definedType: { $case: 'primitiveType', value: VariableType.STR },
          masked: false,
        },
      },
    })
  })
})
