declare module '*.css' {
  // Use this if you are using CSS Modules (recommended)
  const content: { [className: string]: string }
  export default content
}

declare module '*.svg' {
  import { FC, SVGProps } from 'react'

  const ReactComponent: FC<SVGProps<SVGSVGElement>>
  export default ReactComponent
}
