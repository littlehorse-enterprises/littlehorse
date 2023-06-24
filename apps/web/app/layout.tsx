import "../global.scss"
import "ui/styles.scss"
import { Providers } from "../providers";
import { Container } from "ui";
import { signOut } from "next-auth/react";
import { HeaderBar } from "./components/HeaderBar";


export const metadata = {
  title: 'Little Horse',
  description: 'Copyright © 2023 LittleHorse Enterprises LLC. ',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <head>
      <link rel="icon" type="image/x-icon" href="/littlehorse.svg" />
      </head>
      <body>
        <Providers >
          <HeaderBar />
          <Container>
            {children}
          </Container>
        </Providers>
      </body>
    </html>
  );
}
