import { MetadataSearch } from './(main)/sections/MetadataSearch'
import { Metrics } from './(main)/sections/Metrics'

export default function Page() {

    return (
        <>
            <h1>Cluster Overview </h1>

            <Metrics />
            <MetadataSearch />
        </>
    )
}
