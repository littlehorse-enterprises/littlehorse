import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  output: "standalone",
  /* config options here */
  env: {
    VERSION: "dev",
  }
}

export default nextConfig
