import { cn } from '@/lib/utils'
import { LucideIcon } from 'lucide-react'
import { FC, PropsWithChildren, ReactNode } from 'react'

const CAPTION_MAX_WIDTH_PX = 88
const CAPTION_MAX_FONT_PX = 8
const CAPTION_MIN_FONT_PX = 5.5
const CAPTION_CHAR_WIDTH_PX = 4.6

const fittingCaptionFontSize = (
  text: string,
  maxWidthPx = CAPTION_MAX_WIDTH_PX,
  maxFontPx = CAPTION_MAX_FONT_PX,
  minFontPx = CAPTION_MIN_FONT_PX
) => {
  const neededWidth = text.length * CAPTION_CHAR_WIDTH_PX
  if (neededWidth <= maxWidthPx) return maxFontPx
  const scaled = maxFontPx * (maxWidthPx / neededWidth)
  return Math.max(minFontPx, scaled)
}

const FittingCaption: FC<{
  text: string
  className?: string
  maxWidthPx?: number
  maxFontPx?: number
  minFontPx?: number
  measureText?: string
}> = ({ text, className, maxWidthPx, maxFontPx, minFontPx, measureText }) => (
  <span
    className={cn('block max-w-[5.5rem] text-center leading-tight whitespace-nowrap', className)}
    style={{
      fontSize: `${fittingCaptionFontSize(measureText ?? text, maxWidthPx, maxFontPx, minFontPx)}px`,
    }}
    title={text}
  >
    {text}
  </span>
)

export type NodeChromeTheme = {
  labelClass: string
  iconClass: string
  borderClass: string
  bgClass: string
  selectedBgClass: string
  textClass: string
}

type DiagramNodeShellProps = PropsWithChildren<{
  id: string
  label: string
  icon: LucideIcon
  theme: Pick<NodeChromeTheme, 'labelClass' | 'iconClass'>
  subtitle?: string
}>

export const DiagramNodeShell: FC<DiagramNodeShellProps> = ({
  id,
  label,
  icon: Icon,
  theme,
  subtitle,
  children,
}) => (
  <div className="group relative inline-block">
    <div className="pointer-events-none absolute bottom-full left-1/2 z-10 mb-0.5 inline-flex -translate-x-1/2 items-center justify-center gap-0.5 whitespace-nowrap">
      <Icon className={cn('h-2.5 w-2.5 shrink-0', theme.iconClass)} strokeWidth={2} />
      <span
        className={cn(
          'text-center text-[9px] font-semibold uppercase leading-tight tracking-wider',
          theme.labelClass
        )}
        title={label}
      >
        {label}
      </span>
    </div>
    <div className="relative inline-block">{children}</div>
    {subtitle ? (
      <div className="pointer-events-none absolute top-full left-1/2 z-10 mt-0.5 -translate-x-1/2">
        <FittingCaption text={subtitle} className="text-slate-700" />
      </div>
    ) : null}
    <span
      className="pointer-events-none invisible absolute top-full left-1/2 z-10 mt-0.5 -translate-x-1/2 pt-0.5 text-center font-mono text-[7px] leading-tight whitespace-nowrap text-slate-500 group-hover:visible"
      style={{ marginTop: subtitle ? '0.85rem' : undefined }}
      title={id}
    >
      {id}
    </span>
  </div>
)

type DiagramNodeCardProps = PropsWithChildren<{
  selected?: boolean
  theme: NodeChromeTheme
  className?: string
}>

export const DiagramNodeCard: FC<DiagramNodeCardProps> = ({ selected, theme, className, children }) => (
  <div
    className={cn(
      'relative min-w-[3.25rem] cursor-pointer rounded-md border-[1px] px-2 py-1.5 text-xs',
      theme.borderClass,
      theme.bgClass,
      selected && theme.selectedBgClass,
      className
    )}
  >
    <div className={cn('block truncate px-1 text-center font-medium leading-tight', theme.textClass)}>{children}</div>
  </div>
)

type DiagramNodeCircleProps = {
  selected?: boolean
  theme: NodeChromeTheme
  icon: LucideIcon
  iconClass?: string
  sizeClass?: string
}

export const DiagramNodeCircle: FC<DiagramNodeCircleProps> = ({
  selected,
  theme,
  icon: Icon,
  iconClass,
  sizeClass = 'h-10 w-10',
}) => (
  <div
    className={cn(
      'relative flex cursor-pointer items-center justify-center rounded-full border-[1px]',
      sizeClass,
      theme.borderClass,
      theme.bgClass,
      selected && theme.selectedBgClass
    )}
  >
    <Icon className={cn('h-4 w-4', iconClass ?? theme.iconClass)} strokeWidth={2} />
  </div>
)

export const DiagramNodeSubtitle: FC<{ children: ReactNode }> = ({ children }) => (
  <FittingCaption text={String(children)} className="text-slate-700" />
)

const DIAMOND_CLIP = '[clip-path:polygon(50%_0,100%_50%,50%_100%,0_50%)]'

type DiagramNodeDiamondProps = PropsWithChildren<{
  selected?: boolean
  theme: NodeChromeTheme
  sizeClass?: string
  innerInsetClass?: string
}>

export const DiagramNodeDiamond: FC<DiagramNodeDiamondProps> = ({
  selected,
  theme,
  sizeClass = 'h-10 w-10',
  innerInsetClass = 'inset-[2px]',
  children,
}) => (
  <div className={cn('relative grid cursor-pointer place-items-center', sizeClass)}>
    <div className={cn('absolute inset-0', theme.borderClass.replace('border-', 'bg-'), DIAMOND_CLIP)} />
    <div
      className={cn(
        'absolute',
        innerInsetClass,
        theme.bgClass,
        selected && theme.selectedBgClass,
        DIAMOND_CLIP
      )}
    />
    <div className="relative z-10 flex h-full w-full items-center justify-center">{children}</div>
  </div>
)
