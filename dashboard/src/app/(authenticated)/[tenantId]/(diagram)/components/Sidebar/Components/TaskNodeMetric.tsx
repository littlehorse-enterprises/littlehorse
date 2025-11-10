type TaskNodeMetricProps = {
  value: React.ReactNode
  title: string
  measure?: string
}

export const TaskNodeMetric = ({ value, title, measure }: TaskNodeMetricProps) => {
  return (
    <div className="flex flex-1 flex-col">
      <small className="text-[0.75em] text-slate-400">{title}</small>
      <p className="text-lg font-medium">
        {value}
        {measure && <span className="text-sm text-slate-400">{measure}</span>}{' '}
      </p>
    </div>
  )
}
