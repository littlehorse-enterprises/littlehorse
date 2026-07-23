import { test, expect } from '@playwright/test'

/**
 * Cheap, high-leverage invariants applied to every key page. These require no foresight
 * about a specific bug: most of the recent protobuf-ts migration regressions surfaced as
 * "[object Object]" (a proto object rendered as text) or a console error. Asserting their
 * absence across the app is the automatic safety net.
 */
const PAGES = [
  '/default',
  '/default/wfSpec/dashe2e-basic',
  '/default/wfSpec/dashe2e-conditionals',
  '/default/wfRun/dashe2e-basic-completed',
  '/default/wfRun/dashe2e-cond-hi',
  '/default/wfRun/dashe2e-usertask-running',
]

// Known-benign console noise (e.g. aborted server-action fetches during navigation).
const IGNORED_CONSOLE = [/net::ERR_ABORTED/, /Failed to load resource/]

for (const path of PAGES) {
  test(`renders cleanly: ${path}`, async ({ page }) => {
    const errors: string[] = []
    page.on('console', m => {
      if (m.type() === 'error' && !IGNORED_CONSOLE.some(re => re.test(m.text()))) errors.push(m.text())
    })
    page.on('pageerror', e => errors.push(String(e)))

    await page.goto(path, { waitUntil: 'domcontentloaded' })
    // wait for the app shell (present on every authenticated page)
    await page.getByPlaceholder(/Open WfRun by ID/i).waitFor()

    const body = await page.locator('body').innerText()
    expect(body, `"[object Object]" leaked into ${path}`).not.toContain('[object Object]')
    expect(body, `"NaN" leaked into ${path}`).not.toContain('NaN')
    expect(errors, `console errors on ${path}`).toEqual([])
  })
}
