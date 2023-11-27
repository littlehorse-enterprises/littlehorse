const webpack = require('webpack')
const flags = require('./featureToggles.js')
const serializedFlags = Object.keys(flags).reduce((acc, key) => {
    acc[key] = JSON.stringify(flags[key])
    return acc
}, {})

module.exports = {
    webpack(config) {
        config.plugins.push(new webpack.DefinePlugin(serializedFlags))
        return config
    },
    reactStrictMode: true,
    transpilePackages: [ 'ui' ],
    sassOptions: {
        outputStyle: 'expanded',
    },
}
