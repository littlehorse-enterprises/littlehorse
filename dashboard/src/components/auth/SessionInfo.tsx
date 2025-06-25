import { auth } from '@/auth'

export default async function SessionInfo() {
  const session = await auth()

  return (
    <div className="rounded-lg border bg-gray-50 p-4 dark:bg-gray-900">
      <h2 className="mb-4 text-xl font-bold">Session Information</h2>
      <div className="space-y-2">
        <div>
          <span className="font-semibold">Status: </span>
          <span className={session ? 'text-green-600' : 'text-red-600'}>
            {session ? 'Authenticated' : 'Not Authenticated'}
          </span>
        </div>
        {session && (
          <>
            <div>
              <span className="font-semibold">User: </span>
              <span>{session.user?.name || session.user?.email}</span>
            </div>
            <div>
              <span className="font-semibold">Email: </span>
              <span>{session.user?.email}</span>
            </div>
            <div>
              <span className="font-semibold">Session Expires: </span>
              <span>{new Date(session.expires).toLocaleString()}</span>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
