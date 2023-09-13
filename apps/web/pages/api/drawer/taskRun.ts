import { NextApiRequest, NextApiResponse } from "next";

export default async function handler(
  req: NextApiRequest,
  res: NextApiResponse
) {
  if (req.method === "POST") {
    console.log(req.body)
    const body = JSON.parse(req.body);
    const { wfRunId, taskGuid } = body;
    const raw = await fetch(
      `${process.env.API_URL}/taskRun/${wfRunId}/${taskGuid}`
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
