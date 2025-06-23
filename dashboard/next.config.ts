import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  output: "standalone",
  env: {
    VERSION: "dev",
  }
}

export default nextConfig
