export const parseValueByType = (value:any) => {
    if (value?.jsonObj != undefined) {return (value?.jsonObj)}
    if (value?.jsonArr != undefined) {return (value?.jsonArr)}
    if (value?.double != undefined) {return (value?.double)}
    if (value?.bool != undefined) {return JSON.stringify(value?.bool)}
    if (value?.int != undefined) {return (value?.int)}
    if (value?.bytes != undefined) {return (value?.bytes)}
    return value?.str
}
