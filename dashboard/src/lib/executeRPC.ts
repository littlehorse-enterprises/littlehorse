import { LittleHorseDefinition } from "littlehorse-client/proto";
import { LucideIcon } from "lucide-react";
import { lhClient } from "./lhClient";

export type LittleHorseMethodRPCName =
  keyof typeof LittleHorseDefinition.methods;
export type LittleHorseMethod =
  (typeof LittleHorseDefinition.methods)[LittleHorseMethodRPCName];
export type LittleHorseMethodName = LittleHorseMethod["name"];
export type LittleHorseMethodInput = LittleHorseMethod["requestType"];
export type LittleHorseMethodOutput = LittleHorseMethod["responseType"];

export type RpcMethod = {
  name: LittleHorseMethodName;
  inputType: LittleHorseMethodInput;
  outputType: LittleHorseMethodOutput;
  rpcName: LittleHorseMethodRPCName;
  icon: LucideIcon;
};

export type LHMethodParamType<M extends LittleHorseMethodRPCName> = ReturnType<
  (typeof LittleHorseDefinition.methods)[M]["requestType"]["create"]
>;
export type LHMethodReturnType<M extends LittleHorseMethodRPCName> = ReturnType<
  (typeof LittleHorseDefinition.methods)[M]["responseType"]["create"]
>;
