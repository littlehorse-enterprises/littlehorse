import { NextApiRequest, NextApiResponse } from "next";

export default async function handler(
  req: NextApiRequest,
  res: NextApiResponse
) {
  if (req.method === "POST") {
    const body = JSON.parse(req.body);
    const { wfRunId, guid, externalEventDefName } = body;
    const raw = await fetch(
      `${process.env.API_URL}/externalEvent/${wfRunId}/${externalEventDefName}/${guid}`
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
