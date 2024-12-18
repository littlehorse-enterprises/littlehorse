import { FC, useEffect, useRef, useState } from 'react'
import { cn } from '@/components/utils'
import { Button } from '@/components/ui/button'
import { ChevronRight } from 'lucide-react'
import {
    Dialog,
    DialogContent,
    DialogTrigger,
} from '@/components/ui/dialog'
import { CopyButton } from './copy-button'

type OverflowTextProps = {
    text: string
    className?: string
}

export function formatAsJson(text: string): string {
    try {
        const parsed = JSON.parse(text);
        return JSON.stringify(parsed, null, 4);
    } catch {
        return text;
    }
}

export const OverflowText: FC<OverflowTextProps> = ({ text, className }) => {
    const textRef = useRef<HTMLDivElement>(null)
    const [isOverflowing, setIsOverflowing] = useState(false)

    useEffect(() => {
        const element = textRef.current
        if (element) {
            setIsOverflowing(element.scrollWidth > element.clientWidth)
        }
    }, [text])

    const formattedText = formatAsJson(text)

    if (isOverflowing) {
        return (
            <Dialog>
                <DialogTrigger asChild>
                    <Button
                        variant="ghost"
                        className={cn("w-full truncate flex justify-between items-center p-1 h-auto font-normal hover:no-underline", className)}
                    >
                        <span className="truncate">
                            {formattedText}
                        </span>
                        <div className="flex items-center gap-1 flex-shrink-0 text-xs text-muted-foreground">
                            View More
                            <ChevronRight className="h-4 w-4 opacity-50" />
                        </div>
                    </Button>
                </DialogTrigger>
                <DialogContent className="max-w-2xl overflow-visible gap-2">
                    <CopyButton
                        value={formattedText}
                        className="h-8 w-8 rounded-full"
                    />
                    <div className="h-96 overflow-auto bg-gray-100 rounded-lg">
                        <div className="max-w-full break-words whitespace-pre-wrap p-4">
                            {formattedText}
                        </div>
                    </div>
                </DialogContent>
            </Dialog>
        )
    }
    return (
        <div
            ref={textRef}
            className={cn('truncate whitespace-nowrap overflow-hidden', className)}
        >
            {formattedText}
        </div>
    )
}