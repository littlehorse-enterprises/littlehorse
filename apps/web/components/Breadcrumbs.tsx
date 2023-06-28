// import Breadcrumb from '@/app/utils/dataTypes/breaadcrumb';
import Link from 'next/link';
import React from 'react';

interface Breadcrumb {
    title: string,
    href?: string,
    active?: boolean
}

export interface Props {
    pwd: Breadcrumb[];
}

const Breadcrumbs = ({
    pwd
}: Props) => {
    const display = (): React.ReactNode[] => {
        const nodes: React.ReactNode[] = [];
        return pwd.map(directory => {
            // console.log('directory', directory);
            
            if (directory.active) {
                return (<span key={directory.title}>{directory.title}</span>)
            }
           return (<><span className='link' key={directory.title}><Link href={directory.href || '/'}>{directory.title}</Link></span> / </>)
        });

        //return nodes;
    }

    return <div className='breadcrumb'>
        {
            display()
        }
    </div>
}

export default Breadcrumbs;
