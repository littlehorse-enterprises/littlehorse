import {
  ACLAction,
  ACLResource,
  Principal,
  ServerACLs,
} from "littlehorse-client/proto";

export function getTenants({
  perTenantAcls,
  globalAcls,
}: Pick<Principal, "globalAcls" | "perTenantAcls">): string[] {
  let tenants: string[] = [];
  if (globalAcls && hasDefaultAccess(globalAcls)) {
    tenants = ["default"];
  }
  return [...tenants, ...Object.keys(perTenantAcls)];
}

function hasDefaultAccess({ acls }: ServerACLs): boolean {
  const result = acls.filter(({ resources, allowedActions }) => {
    return (
      resources.includes(ACLResource.ACL_ALL_RESOURCES) &&
      allowedActions.includes(ACLAction.ALL_ACTIONS)
    );
  });
  return result.length > 0;
}
