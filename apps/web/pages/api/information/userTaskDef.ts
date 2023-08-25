import { NextApiRequest, NextApiResponse } from "next";

export default async function handler(
    req: NextApiRequest,
    res: NextApiResponse
) {
    if (req.method === "POST") {
        const body = JSON.parse(req.body);
        const { id } = body;
        const { version } = body;
        const raw = await fetch(
            process.env.API_URL + "/userTaskDef/" + id + "/" + version
        );
        if (raw.ok) {
            const content = await raw.json();
            return res.send(content);
        }
        res.send({
            error: "Something goes wrong.",
        });
    }
}
