export const Divider = ({ title }: { title: string }) => {
  return (
    <div className="my-2 flex w-full items-center">
      <div className="h-px flex-grow bg-gray-300"></div>
      <p className="node-title mx-4">{title}</p>
      <div className="h-px flex-grow bg-gray-300"></div>
    </div>
  )
}
