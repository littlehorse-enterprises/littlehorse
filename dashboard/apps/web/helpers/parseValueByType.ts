export const parseValueByType = (value:any) => {
    if (value?.type === 'JSON_OBJ') {return (value?.jsonObj)}
    if (value?.type === 'JSON_ARR') {return (value?.jsonArr)}
    if (value?.type === 'DOUBLE') {return (value?.double)}
    if (value?.type === 'BOOL') {return JSON.stringify(value?.bool)}
    if (value?.type === 'INT') {return (value?.int)}
    if (value?.type === 'BYTES') {return (value?.bytes)}
    return value?.str
}