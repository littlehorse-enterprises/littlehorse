import { defineConfig, devices } from '@playwright/test'

/**
 * Dashboard end-to-end tests. These run a real browser against a real Dashboard that is
 * talking to a real LittleHorse server seeded with deterministic fixtures
 * (see `examples/java/dashboard-e2e-fixtures` and `e2e/README.md`).
 *
 * Env:
 *   E2E_BASE_URL  Dashboard URL (default http://localhost:3000)
 *   LHC_API_HOST / LHC_API_PORT  LittleHorse server the Dashboard talks to (default localhost:2023)
 */
const baseURL = process.env.E2E_BASE_URL ?? 'http://localhost:3000'

export default defineConfig({
  testDir: './tests',
  globalSetup: './global-setup.ts',
  // Fixtures are shared and read-only for most specs; specs that create runs use unique ids.
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: process.env.CI ? 2 : undefined,
  reporter: process.env.CI ? [['github'], ['html', { open: 'never' }]] : [['list'], ['html', { open: 'never' }]],
  timeout: 30_000,
  expect: { timeout: 10_000 },

  use: {
    baseURL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },

  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    // Enabled in the nightly matrix, not on every PR:
    // { name: 'firefox', use: { ...devices['Desktop Firefox'] } },
    // { name: 'webkit', use: { ...devices['Desktop Safari'] } },
    // { name: 'mobile', use: { ...devices['Pixel 7'] } },
  ],

  // Convenience for local runs: start the dashboard if one isn't already up. In CI the
  // workflow starts the dashboard and sets E2E_BASE_URL, and this is reused.
  webServer: {
    command: 'pnpm run dev',
    cwd: '..',
    url: `${baseURL}/default`,
    reuseExistingServer: true,
    timeout: 120_000,
    env: {
      NEXTAUTH_SECRET: process.env.NEXTAUTH_SECRET ?? 'e2e-secret',
      NEXTAUTH_URL: baseURL,
      LHC_API_HOST: process.env.LHC_API_HOST ?? 'localhost',
      LHC_API_PORT: process.env.LHC_API_PORT ?? '2023',
    },
  },
})
