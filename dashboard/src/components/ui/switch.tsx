import React from 'react'
import * as Switch from '@radix-ui/react-switch'

export const SwitchButton = ({ props }: any) => {
  const { title } = props
  return (
    <form>
      <div className="flex items-center">
        {title && (
          <label className="pr-[15px] text-[15px] leading-none text-white" htmlFor="airplane-mode">
            Airplane mode
          </label>
        )}
        <Switch.Root
          className="bg-blackA6 shadow-blackA4 relative h-[25px] w-[42px] cursor-default rounded-full shadow-[0_2px_10px] outline-none focus:shadow-[0_0_0_2px] focus:shadow-black data-[state=checked]:bg-black"
          id="airplane-mode"
        >
          <Switch.Thumb className="shadow-blackA4 block size-[21px] translate-x-0.5 rounded-full bg-white shadow-[0_2px_2px] transition-transform duration-100 will-change-transform data-[state=checked]:translate-x-[19px]" />
        </Switch.Root>
      </div>
    </form>
  )
}
