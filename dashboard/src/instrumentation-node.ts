import { context } from '@opentelemetry/api'
import { AsyncLocalStorageContextManager } from '@opentelemetry/context-async-hooks'
import { getRPCMetadata, RPCType, setRPCMetadata } from '@opentelemetry/core'
import { PrometheusExporter } from '@opentelemetry/exporter-prometheus'
import { HostMetrics } from '@opentelemetry/host-metrics'
import { registerInstrumentations } from '@opentelemetry/instrumentation'
import { HttpInstrumentation } from '@opentelemetry/instrumentation-http'
import { RuntimeNodeInstrumentation } from '@opentelemetry/instrumentation-runtime-node'
import { detectResourcesSync, envDetector, hostDetector, processDetector, Resource } from '@opentelemetry/resources'
import { MeterProvider } from '@opentelemetry/sdk-metrics'
import { ATTR_SERVICE_NAME, ATTR_SERVICE_VERSION } from '@opentelemetry/semantic-conventions'
import { IncomingMessage } from 'http'

// manually setting a context manager to replace the no-op context manager
context.setGlobalContextManager(new AsyncLocalStorageContextManager())

const exporter = new PrometheusExporter({
  port: 9464,
})
const detectedResources = detectResourcesSync({
  detectors: [envDetector, processDetector, hostDetector],
})

const customResources = new Resource({
  [ATTR_SERVICE_NAME]: 'LH Dashboard',
  [ATTR_SERVICE_VERSION]: '0.1.0',
})

const resources = detectedResources.merge(customResources)

const meterProvider = new MeterProvider({
  readers: [exporter],
  resource: resources,
})
const hostMetrics = new HostMetrics({
  name: 'olo-r:shell',
  meterProvider,
})

registerInstrumentations({
  meterProvider,
  instrumentations: [
    new HttpInstrumentation({
      requestHook: (span, request) => {
        const route = (request as IncomingMessage)?.url
        if (route) {
          if (route && (route.endsWith('.json') || !route.includes('.'))) {
            // Try to apply the route only for pages and client side fetches
            const rpcMetadata = getRPCMetadata(context.active()) // retrieve rpc metadata from the active context
            if (rpcMetadata) {
              if (rpcMetadata?.type === RPCType.HTTP) {
                rpcMetadata.route = route
              }
            } else {
              setRPCMetadata(context.active(), {
                type: RPCType.HTTP,
                route,
                span,
              })
            }
          }
        }
      },
    }),
    new RuntimeNodeInstrumentation(),
  ],
})
hostMetrics.start()
