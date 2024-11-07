import * as AccordionRedux from '@radix-ui/react-accordion'
import { ChevronDownIcon } from 'lucide-react'

import React, { FC, ReactNode } from 'react'
type Prop = {
  items: { title: string | ReactNode; content: string | ReactNode }[]
}

export const Accordion: FC<Prop> = ({ items }) => {
  return (
    <AccordionRedux.Root type="single" collapsible className="w-full space-y-2">
      {items?.map((item, index) => (
        <AccordionRedux.Item key={index} value={`item-${index}`} className="overflow-hidden rounded-lg border">
          <AccordionRedux.Header className="w-full">
            <AccordionRedux.Trigger className="flex w-full items-center justify-between bg-gray-100 px-4 py-2 text-left hover:bg-gray-200 focus:outline-none">
              <span className="mr-2 w-full font-medium">{item.title}</span>
              <ChevronDownIcon className="group-radix-state-open:rotate-180 h-5 w-5 transform transition-transform duration-300 ease-in-out" />
            </AccordionRedux.Trigger>
          </AccordionRedux.Header>
          <AccordionRedux.Content className="bg-white px-4 py-2 text-gray-700">{item.content}</AccordionRedux.Content>
        </AccordionRedux.Item>
      ))}
    </AccordionRedux.Root>
  )
}

export default Accordion
