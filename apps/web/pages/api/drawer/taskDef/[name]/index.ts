import { NextApiRequest, NextApiResponse } from 'next'
import { getServerSession } from 'next-auth/next'
import { authOptions } from '../../../auth/[...nextauth]'

export default async function handler(
	req: NextApiRequest,
	res: NextApiResponse
) {
	const session = await getServerSession(req, res, authOptions)
	if (session) {
		if (req.method === 'GET') {
			const url = `${process.env.API_URL}/taskDef/${req.query.name}`

			const response = await fetch(url)
			const data = await response.json()

			return res.json({
				code: 'OK',
				data
			})
		}
	} else {
		res.send({
			error: 'You must be signed in to view the protected content on this page.'
		})
	}
}
