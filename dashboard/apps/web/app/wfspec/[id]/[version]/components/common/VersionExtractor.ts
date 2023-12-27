export const getVersionFromFormattedString = (formattedVersion: string) => {
    const versionValues = formattedVersion.split('.')
    return {
        majorVersion: versionValues[0],
        revision: versionValues[1]
    }
}
