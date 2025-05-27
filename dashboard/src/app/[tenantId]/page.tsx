import { PageParams } from "@/types/PageParams";

export default async function DashboardPage({
    params,
    searchParams,
}: PageParams) {
    // Access filters parameter
    const param = (await params);
    console.log(param);

    return <div>Dashboard</div>;
}
