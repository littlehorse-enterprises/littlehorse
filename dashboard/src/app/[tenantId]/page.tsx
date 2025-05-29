import { MetadataSearchClient } from "./components/metadata-search-client"

export default function MetadataSearchPage() {
    return (
        <div className="flex-1 overflow-auto container mx-auto py-6">
            <MetadataSearchClient />
        </div>
    )
}