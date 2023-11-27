import '../global.scss'
import 'ui/styles.scss'
import 'material-icons/iconfont/material-icons.css'
import { Container } from 'ui'
import Head from 'next/head'
import { Providers } from '../providers'
import { HeaderBar } from './(main)/components/HeaderBar'


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
            <Head>
                <link href="/littlehorse.svg" rel="icon" type="image/x-icon" />
            </Head>
            <body>
                <Providers>
                    <HeaderBar />
                    <Container>
                        {children}
                    </Container>
                </Providers>
            </body>
        </html>
    )
}
