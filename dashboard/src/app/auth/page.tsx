import SignIn from '@/components/auth/signIn'
import SessionInfo from '@/components/auth/SessionInfo'

export default function Home() {
  // this is a test page
  return (
    <div className="grid min-h-screen grid-rows-[20px_1fr_20px] items-center justify-items-center gap-16 p-8 pb-20 font-[family-name:var(--font-geist-sans)] sm:p-20">
      <main className="row-start-2 flex flex-col items-center gap-8 sm:items-start">
        <SignIn />
        <SessionInfo />
      </main>
    </div>
  )
}
