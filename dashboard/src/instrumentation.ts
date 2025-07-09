export const register = async () => {
  if (process.env.NEXT_RUNTIME === 'nodejs' && process.env.LHD_METRICS_DISABLED !== 'true') {
    await import('./instrumentation-node')
  }
}
