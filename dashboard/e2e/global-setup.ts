import { chromium, type FullConfig } from '@playwright/test'

/**
 * Fail fast (with a helpful message) if the deterministic fixtures aren't seeded, so a
 * missing-fixtures run doesn't show up as dozens of confusing assertion failures.
 */
async function globalSetup(config: FullConfig) {
  const baseURL = config.projects[0]?.use?.baseURL ?? 'http://localhost:3000'
  const browser = await chromium.launch()
  const page = await browser.newPage()
  try {
    await page.goto(`${baseURL}/default/wfRun/dashe2e-basic-completed`, { waitUntil: 'domcontentloaded' })
    await page.getByText('COMPLETED').first().waitFor({ timeout: 30_000 })
  } catch {
    throw new Error(
      'E2E fixtures are not seeded (could not find wfRun "dashe2e-basic-completed" as COMPLETED).\n' +
        'Start the stack and seed fixtures first:  pnpm e2e:up   (see dashboard/e2e/README.md)'
    )
  } finally {
    await browser.close()
  }
}

export default globalSetup
