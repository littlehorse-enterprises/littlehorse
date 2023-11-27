const featureToggles = require('./apps/web/featureToggles')

Object.keys(featureToggles).forEach((key) => {
    global[key] = featureToggles[key]
})
