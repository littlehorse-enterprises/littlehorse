export const NodeVariable = ({ label, text }: { label: string; text: string }) => {
  return (
    <div className="flex ml-1">
      <div className="grow text-sm">{label}</div>
      <div className="truncate grow text-xs">{text}</div>
    </div>
  )
}
