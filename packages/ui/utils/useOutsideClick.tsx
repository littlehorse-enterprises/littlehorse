"use client"

import { useEffect, useRef } from "react";

// this hook sets an alement with its ancenstor as an anchor to trigger a handler when the user clicks outside of it.
// the ancestor ref was created to avoid any loop if the component already has a handler.

export const useOutsideClick = (
  callback: () => void,
  ancestorRef: React.RefObject<HTMLDivElement> | null
): React.RefObject<HTMLDivElement> => {
  const ref = useRef<HTMLDivElement>(null);

  const handleClick = (event: MouseEvent) => {
    if (
      ref.current &&
      !ref.current.contains(event.target as Node) &&
      ancestorRef &&
      ancestorRef.current &&
      !ancestorRef.current.contains(event.target as Node)
    ) {
      callback();
    }
  };

  useEffect(() => {
    document.addEventListener("mousedown", handleClick);
    return () => {
      document.removeEventListener("mousedown", handleClick);
    };
  }, []);

  return ref;
};

export default useOutsideClick;
