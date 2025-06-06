import SignIn from "@/components/auth/signIn";
import SessionInfo from "@/components/auth/SessionInfo";

export default function Home() {
  // this is a test page
  return (
    <div className="grid grid-rows-[20px_1fr_20px] items-center justify-items-center min-h-screen p-8 pb-20 gap-16 sm:p-20 font-[family-name:var(--font-geist-sans)]">
      <main className="flex flex-col gap-8 row-start-2 items-center sm:items-start">
        <SignIn />
        <SessionInfo />
      </main>
    </div>
  );
}
