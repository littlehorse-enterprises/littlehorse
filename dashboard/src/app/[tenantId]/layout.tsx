import Header from '@/components/header/header'

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <>
      <Header />
      <main className="h-[calc(100vh-64px)]">{children}</main>
    </>
  )
}
