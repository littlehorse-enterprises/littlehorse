declare module '*.css' {
  // Use this if you are using CSS Modules (recommended)
  const content: { [className: string]: string }
  export default content
}
