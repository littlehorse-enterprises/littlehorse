package cmd

import (
	"log"
	"os"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
	"google.golang.org/protobuf/encoding/protojson"
	"google.golang.org/protobuf/proto"
)

var putPrincipalCmd = &cobra.Command{
	Use:   "principal [id]",
	Short: "Create principal.",
	Run: func(cmd *cobra.Command, args []string) {
		acl, _ := cmd.Flags().GetString("acl")
		tenantId, _ := cmd.Flags().GetString("tenantId")
		overwrite, _ := cmd.Flags().GetBool("overwrite")
		id := args[0]
		serverAcls := []*model.ServerACL{}
		per_tenant_acls := make(map[string]*model.ServerACLs)
		for resource, actions := range parseAcl(acl) {
			allowedResources := []model.ACLResource{resource}
			serverAcl := model.ServerACL{
				Resources:      allowedResources,
				AllowedActions: actions,
			}
			serverAcls = append(serverAcls, &serverAcl)
		}

		per_tenant_acls[tenantId] = &model.ServerACLs{
			Acls: serverAcls,
		}

		putRequest := model.PutPrincipalRequest{
			Id:            id,
			PerTenantAcls: per_tenant_acls,
			Overwrite:     overwrite,
		}
		common.PrintResp(getGlobalClient(cmd).PutPrincipal(
			requestContext(cmd),
			&putRequest,
		))
	},
}

var deployPrincipalCmd = &cobra.Command{
	Use:   "principal <file>",
	Short: "Deploy Principal from a file",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("Must provide one arg: the file of the principal to deploy")
		}
		putPrincipalReq := &model.PutPrincipalRequest{}

		// First, read the file
		dat, err := os.ReadFile(args[0])
		if err != nil {
			log.Fatal("Failed to read file: ", err)
		}

		useProto, err := cmd.Flags().GetBool("proto")
		if err != nil {
			log.Fatal("Unexpected error: ", err)
		}
		if useProto {
			err = proto.Unmarshal(dat, putPrincipalReq)
		} else {
			err = protojson.Unmarshal(dat, putPrincipalReq)
		}
		if err != nil {
			log.Fatal("Failed reading deploy file: " + err.Error())
		}

		common.PrintResp(getGlobalClient(cmd).PutPrincipal(requestContext(cmd), putPrincipalReq))
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
	actionsMap = map[string]model.ACLAction{
		"read":  model.ACLAction_READ,
		"run":   model.ACLAction_RUN,
		"write": model.ACLAction_WRITE_METADATA,
		"all":   model.ACLAction_ALL_ACTIONS,
	}
	entitiesMap = map[string]model.ACLResource{
		"acl_workflow":       model.ACLResource_ACL_WORKFLOW,
		"acl_task":           model.ACLResource_ACL_TASK,
		"acl_external_event": model.ACLResource_ACL_EXTERNAL_EVENT,
		"acl_user_task":      model.ACLResource_ACL_USER_TASK,
		"acl_principal":      model.ACLResource_ACL_PRINCIPAL,
		"acl_tenant":         model.ACLResource_ACL_TENANT,
		"all":                model.ACLResource_ACL_ALL_RESOURCES,
	}
)

var deletePrincipalCmd = &cobra.Command{
	Use:   "principal <id>",
	Short: "Delete a Principal.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the ID of Principal to delete.")

		}

		common.PrintResp(getGlobalClient(cmd).DeletePrincipal(
			requestContext(cmd),
			&model.DeletePrincipalRequest{
				Id: &model.PrincipalId{
					Id: args[0],
				},
			},
		))
	},
}

func init() {
	putCmd.AddCommand(putPrincipalCmd)
	putPrincipalCmd.Flags().String("acl", "", "ACLs")
	putPrincipalCmd.Flags().Bool("overwrite", false, "Overwrites principal information")
	putPrincipalCmd.Flags().String("tenantId", "", "Tenant associated with the principal")

	deployCmd.AddCommand(deployPrincipalCmd)
	deleteCmd.AddCommand(deletePrincipalCmd)
}
