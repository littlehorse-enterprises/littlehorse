const extractLabel = (nodeName: string) => {
    return nodeName.split('-').slice(1,-1).join('-')
}

const LabelsUtils = {
    extractLabel
}

export default LabelsUtils
