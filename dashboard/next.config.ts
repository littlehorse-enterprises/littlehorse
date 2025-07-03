import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  output: 'standalone',
  env: {
    NEXT_PUBLIC_VERSION: 'v0.0.0-dev',
  },
}

export default nextConfig
