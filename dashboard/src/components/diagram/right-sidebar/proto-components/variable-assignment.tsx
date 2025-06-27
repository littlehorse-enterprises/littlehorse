import { getVariable } from "@/utils/data/variables";
import { VariableAssignment } from "littlehorse-client/proto";

// todo: more complex handling for runs, where other variableAssignment fields will be populated
export function VariableAssignmentComponent(variableAssignment: VariableAssignment) {
    return (
        <div>
            {getVariable(variableAssignment)}
        </div>
    )
}