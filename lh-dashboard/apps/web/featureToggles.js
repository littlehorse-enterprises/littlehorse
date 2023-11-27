const getEnv = () => {
    const env = process.env.NODE_ENV
    if (env === 'development') {return 'development'}
    if (env === 'production') {return 'production'}
    if (env === 'test') {return 'test'}
    // Important: make sure the default case returns the
    // production environment. This is to make sure that if
    // someone forgets to add the environment variable, only
    // the most tested feature flags will be enabled.
    return 'production'
}

const flags = {
    __AUTHENTICATION_ENABLED__: [ 'test' ], // LH-279
}

module.exports = Object.keys(flags).reduce((acc, key) => {
    acc[key] = flags[key].includes(getEnv())
    return acc
}, {})
