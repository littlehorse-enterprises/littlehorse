const webpack = require('webpack')
const flags = require('./featureToggles.js')
const {join} = require('path');

module.exports = {
    webpack(config) {
        config.plugins.push(new webpack.DefinePlugin(flags))
        return config
    },
    reactStrictMode: true,
    transpilePackages: [ 'ui' ],
    sassOptions: {
        outputStyle: 'expanded',
    },
    output: 'standalone',
    outputFileTracingRoot: join(__dirname, '../../'),
}
