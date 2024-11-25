export const getValidation = (type: string) => {
  const validations: Record<string, (value: string) => true | string> = {
    JSON_OBJ: (value: string) => {
      if (!value) return true
      try {
        const parsed = JSON.parse(value)
        if (typeof parsed === 'object' && !Array.isArray(parsed)) {
          return true
        }
        return 'Input must be a valid JSON object'
      } catch {
        return 'Input must be a valid JSON object'
      }
    },
    JSON_ARR: (value: string) => {
      if (!value) return true
      try {
        const parsed = JSON.parse(value)
        if (Array.isArray(parsed) && parsed.every(item => typeof item === 'object')) {
          return true
        }
        return 'Input must be a valid JSON array'
      } catch {
        return 'Input must be a valid JSON array'
      }
    },
  }

  return validations[type]
}
