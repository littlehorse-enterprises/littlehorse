import { NextApiRequest, NextApiResponse } from "next";

export default async function handler(req:NextApiRequest, res:NextApiResponse) {
    

      if(req.method === 'POST'){
        const raw = await fetch(process.env.API_URL+"/metrics/wfSpec",{
            method:'POST',
            body: req.body,
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
        res.send({
            error: "Something goes wrong.",
          })
      }
    

    
    // const session = await getServerSession(req, res, authOptions)

    // if (session) {
    //     res.send({
    //         content:
    //           "This is protected content. You can access this content because you are signed in.",
    //       })
    //   } else {
    //     res.send({
    //       error: "You must be signed in to view the protected content on this page.",
    //     })
    //   }



}