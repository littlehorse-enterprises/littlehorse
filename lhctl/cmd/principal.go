package cmd

import (
	"strings"
	"github.com/spf13/cobra"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
)


var putPrincipalCmd = &cobra.Command{
	Use:   "principal",
	Short: "Create principal.",
	Run: func(cmd *cobra.Command, args []string) {
		acl, _ := cmd.Flags().GetString("acl")
		tenantId, _ := cmd.Flags().GetString("tenantId")
		id := args[0]
		serverAcls := []*model.ServerACL{}
		per_tenant_acls := make(map[string]*model.ServerACLs)
		for resource, actions := range parseAcl(acl){
			allowedResources := []model.ACLResource{resource}
			serverAcl := model.ServerACL{
				Resources: allowedResources,
				AllowedActions: actions,
			}
			serverAcls = append(serverAcls, &serverAcl)
		}

		per_tenant_acls[tenantId] = &model.ServerACLs{
			Acls: serverAcls,
		}

		putRequest := model.PutPrincipalRequest{
			Id: id,
			PerTenantAcls: per_tenant_acls,
		}
		common.PrintResp(getGlobalClient(cmd).PutPrincipal(
			requestContext(),
			&putRequest,
		))
	},
}

func parseAcl(input string) map[model.ACLResource][]model.ACLAction {
	// Split the input string into individual permissions
	permissions := strings.Split(input, ",")

	entityMap := make(map[model.ACLResource][]model.ACLAction)

	// Iterate through each permission and extract workflow/task and access rights
	for _, permission := range permissions {
		parts := strings.Split(permission, ":")
		entity, entity_mapped := entitiesMap[strings.ToLower(parts[0])]
		access, action_mapped := actionsMap[strings.ToLower(parts[1])]
		if entity_mapped && action_mapped {
			// Check if the entity already exists in the result map
			if _, ok := entityMap[entity]; ok {
				// If it exists, append the access right to the existing slice
				entityMap[entity] = append(entityMap[entity], access)
			} else {
				// If it doesn't exist, create a new slice and add the access right to it
				entityMap[entity] = []model.ACLAction{access}
			}
		}
	}
	return entityMap
}

var (
	actionsMap = map[string]model.ACLAction {
		"read": model.ACLAction_READ,
		"run": model.ACLAction_RUN,
		"write": model.ACLAction_WRITE_METADATA,
		"all": model.ACLAction_ALL_ACTIONS,
	}
	entitiesMap = map[string]model.ACLResource {
		"acl_workflow": model.ACLResource_ACL_WORKFLOW,
		"acl_task": model.ACLResource_ACL_TASK,
		"acl_external_event": model.ACLResource_ACL_EXTERNAL_EVENT,
		"acl_user_task": model.ACLResource_ACL_USER_TASK,
		"acl_principal": model.ACLResource_ACL_PRINCIPAL,
		"acl_tenant": model.ACLResource_ACL_TENANT,
		"all": model.ACLResource_ACL_ALL_RESOURCES,
	}
)

func init() {
	putCmd.AddCommand(putPrincipalCmd)
	putPrincipalCmd.Flags().String("acl", "", "ACLs")
	putPrincipalCmd.Flags().String("tenantId", "", "Tenant id")
}
