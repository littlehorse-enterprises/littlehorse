import React from "react";
import Image from "next/image";
import arrowRightSvg from "./arrow-right.svg";

interface DrawerProps {
  title: string;
  children?: React.ReactNode;
}

export const Drawer = (props: DrawerProps) => {
  return (
    <div className="drawer scrollbar">
      <header className="drawer__header">
        <p>{props.title}</p>
      </header>
      {props.children}
    </div>
  );
};
