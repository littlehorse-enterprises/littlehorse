import { NextApiRequest, NextApiResponse } from "next";

export default async function handler(req:NextApiRequest, res:NextApiResponse) {
    

      if(req.method === 'POST'){
        const {
            id
        } = JSON.parse(req.body);

        
        const {
            number, 
            position
        } = req.query;
        
        const raw = await fetch(`${process.env.API_URL}/nodeRun/${id}/${number}/${position}`,{
            mode: 'cors',
            credentials: "include",
            headers: {
                'Content-Type': 'application/json',
                'Accept': '*/*',
            }
        })
        if(raw.ok){
            const content = await raw.json();
            return res.send(content)
        }
        res.status(404);
        res.send({
            error: "Something goes wrong.",
          })
      }
    



}