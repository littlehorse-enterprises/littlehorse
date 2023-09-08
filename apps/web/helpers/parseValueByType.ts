export const parseValueByType = (value:any) => {
    if(value?.type === 'JSON_OBJ') return JSON.stringify(value?.jsonObj)
    if(value?.type === 'JSON_ARR') return JSON.stringify(value?.jsonArr)
    if(value?.type === 'DOUBLE') return JSON.stringify(value?.double)
    if(value?.type === 'BOOL') return JSON.stringify(value?.bool)
    if(value?.type === 'INT') return (value?.int)
    if(value?.type === 'BYTES') return (value?.bytes)
    return value?.str
}