export const nodename = (fullname:string) => {
    if (!fullname) {return ''}
    return fullname.split('-').slice(1,-1).join('-') || ''
}
export const nodeposition = (fullname:string) => {
    if (!fullname) {return 0}
    return fullname.split('-').shift() || 0
}
export const nodetype = (fullname:string) => {
    if (!fullname) {return ''}
    return fullname.split('-').pop() || ''
}