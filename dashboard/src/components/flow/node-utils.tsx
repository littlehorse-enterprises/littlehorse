import { Settings, Circle, Ban, MailCheck, Mail } from "lucide-react"

// Extract node type from node name
export function getNodeType(nodeName: string) {
    const parts = nodeName.split("-");
    return parts.length >= 3 ? parts[parts.length - 1] : "UNKNOWN";
}

// Get icon color based on node type
export function getIconColor(nodeType: string) {
    switch (nodeType) {
        case "TASK":
            return "text-orange-500";
        case "ENTRYPOINT":
            return "text-emerald-500";
        case "EXIT":
            return "text-rose-500";
        case "EXTERNAL_EVENT":
            return "text-blue-400";
        default:
            return "text-gray-500";
    }
}

// Get border color based on node type
export function getBorderColor(nodeType: string) {
    switch (nodeType) {
        case "TASK":
            return "border-orange-500";
        case "ENTRYPOINT":
            return "border-emerald-500";
        case "EXIT":
            return "border-rose-500";
        case "EXTERNAL_EVENT":
            return "border-blue-400";
        default:
            return "border-gray-500";
    }
}

// Get node icon component based on node type
export function getNodeIcon(nodeType: string, size: "sm" | "md" = "md") {
    const iconSize = size === "sm" ? "h-3 w-3" : "h-6 w-6";
    const iconColor = getIconColor(nodeType);

    switch (nodeType) {
        case "TASK":
            return <Settings className={`${iconSize} ${iconColor}`} />
        case "ENTRYPOINT":
            return <Circle className={`${iconSize} ${iconColor}`} />
        case "EXIT":
            return <Ban className={`${iconSize} ${iconColor}`} />
        case "EXTERNAL_EVENT":
            return <Mail className={`${iconSize} ${iconColor}`} />
        default:
            return null
    }
} 