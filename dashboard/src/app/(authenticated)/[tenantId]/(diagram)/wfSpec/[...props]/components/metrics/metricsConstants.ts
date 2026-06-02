import { ChartConfig } from '@/components/ui/chart'

export const TIME_RANGE_OPTIONS = [
  { value: '30', label: 'Last 30 minutes' },
  { value: '60', label: 'Last 1 hour' },
  { value: '360', label: 'Last 6 hours' },
  { value: '720', label: 'Last 12 hours' },
  { value: '1440', label: 'Last 24 hours' },
  { value: '4320', label: 'Last 3 days' },
  { value: '10080', label: 'Last 7 days' },
] as const

export const BUCKET_OPTIONS = [
  { value: '1', label: '1 minute' },
  { value: '5', label: '5 minutes' },
  { value: '10', label: '10 minutes' },
  { value: '30', label: '30 minutes' },
  { value: '60', label: '1 hour' },
  { value: '1440', label: '1 day' },
] as const

export const COUNT_CHART_CONFIG = {
  started: { label: 'Started', color: 'hsl(221, 83%, 53%)' },
  completed: { label: 'Completed', color: 'hsl(142, 71%, 45%)' },
  error: { label: 'Error', color: 'hsl(0, 84%, 60%)' },
  exception: { label: 'Exception', color: 'hsl(38, 92%, 50%)' },
} satisfies ChartConfig

export const LATENCY_CHART_CONFIG = {
  completedAvg: { label: 'Completed (avg)', color: 'hsl(142, 71%, 45%)' },
  completedMax: { label: 'Completed (max)', color: 'hsl(142, 71%, 30%)' },
  errorAvg: { label: 'Error (avg)', color: 'hsl(0, 84%, 60%)' },
  errorMax: { label: 'Error (max)', color: 'hsl(0, 84%, 40%)' },
} satisfies ChartConfig
