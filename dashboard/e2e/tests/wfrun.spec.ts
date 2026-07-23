import { test, expect } from '@playwright/test'

test.describe('WfRun detail', () => {
  test('completed run: status enum, timestamp, and string variable all render', async ({ page }) => {
    await page.goto('/default/wfRun/dashe2e-basic-completed')

    // numeric enum -> rendered as its name, not "1"
    await expect(page.getByText('COMPLETED').first()).toBeVisible()
    // google.protobuf.Timestamp -> a real formatted date, not "[object Object]" or a raw number
    await expect(page.getByText(/\d{1,2}\/\d{1,2}\/\d{4}/).first()).toBeVisible()
    // VariableValue (str) -> its value
    await expect(page.getByText('Ada Lovelace')).toBeVisible()
  })

  test('conditionals run: typed variable values render', async ({ page }) => {
    await page.goto('/default/wfRun/dashe2e-cond-hi')
    await expect(page.getByText('COMPLETED').first()).toBeVisible()

    const body = await page.locator('body').innerText()
    expect(body).toContain('3.14') // double
    expect(body).toContain('processed') // str variable, mutated to "processed" on the taken branch
    expect(body).toContain('42') // int
  })

  test('user-task run parks in RUNNING', async ({ page }) => {
    await page.goto('/default/wfRun/dashe2e-usertask-running')
    await expect(page.getByText('RUNNING').first()).toBeVisible()
  })
})
