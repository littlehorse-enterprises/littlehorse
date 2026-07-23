import { test, expect, type Page } from '@playwright/test'

// The edge-label pill (condition text / "else"). Class-based because the diagram has no
// stable test ids yet; if this drifts, add a data-testid to Default.tsx's pill instead.
const EDGE_PILL = '.react-flow__edgelabel-renderer .rounded-md.bg-gray-200'
const MUTATION_ICON = '.react-flow__edgelabel-renderer .lucide-circle-alert'

async function waitForDiagram(page: Page) {
  await page.locator('.react-flow__node').first().waitFor()
  // reactflow renders edges as SVG <g> that Playwright reports as "hidden"; wait for
  // them to be attached to the DOM rather than visible.
  await page.locator('.react-flow__edge').first().waitFor({ state: 'attached' })
}

test.describe('WfSpec diagram', () => {
  test('conditionals: condition label + else edge + mutation icon, with no empty labels', async ({ page }) => {
    await page.goto('/default/wfSpec/dashe2e-conditionals')
    await waitForDiagram(page)

    const texts = (await page.locator(EDGE_PILL).allInnerTexts()).map(t => t.trim())

    // the condition edge shows the actual condition (references the variable + comparison)
    expect(texts.some(t => t.includes('count') && t.includes('>'))).toBeTruthy()
    // the conditionless sibling is labelled "else"
    expect(texts).toContain('else')
    // regression guard: after the protobuf-ts switch, every edge was rendering an empty pill
    expect(texts.filter(t => t === '')).toHaveLength(0)

    // a variable mutation on an edge renders the alert icon
    await expect(page.locator(MUTATION_ICON).first()).toBeVisible()
  })

  test('basic: a single conditionless edge renders no label pill', async ({ page }) => {
    await page.goto('/default/wfSpec/dashe2e-basic')
    await waitForDiagram(page)
    await expect(page.locator(EDGE_PILL)).toHaveCount(0)
  })
})
