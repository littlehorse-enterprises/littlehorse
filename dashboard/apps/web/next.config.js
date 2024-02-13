const webpack = require('webpack')
const { join } = require('path')

module.exports = {
    reactStrictMode: true,
    transpilePackages: [ 'ui' ],
    sassOptions: {
        outputStyle: 'expanded',
    },
    output: 'standalone',
    outputFileTracingRoot: join(__dirname, '../../'),
}
