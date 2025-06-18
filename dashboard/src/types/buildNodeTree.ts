export interface TreeNode {
  id: string
  label: string
  type?: string
  status?: string
  children: TreeNode[]
  level: number
}
