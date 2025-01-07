package internal

import (
	"log"
	"os"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/spf13/cobra"
	"google.golang.org/protobuf/encoding/protojson"
	"google.golang.org/protobuf/proto"
)

var putPrincipalCmd = &cobra.Command{
	Use:   "principal <id>",
	Short: "Create a principal",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		acl, _ := cmd.Flags().GetString("acl")
		tenantId, _ := cmd.Flags().GetString("tenantId")
		overwrite, _ := cmd.Flags().GetBool("overwrite")
		id := args[0]
		serverAcls := []*lhproto.ServerACL{}
		per_tenant_acls := make(map[string]*lhproto.ServerACLs)

		if cmd.Flags().Lookup("acl").Changed {
			for resource, actions := range parseAcl(acl) {
				allowedResources := []lhproto.ACLResource{resource}
				serverAcl := lhproto.ServerACL{
					Resources:      allowedResources,
					AllowedActions: actions,
				}
				serverAcls = append(serverAcls, &serverAcl)
			}
		}

		if cmd.Flags().Lookup("tenantId").Changed {
			per_tenant_acls[tenantId] = &lhproto.ServerACLs{
				Acls: serverAcls,
			}
		}

		putRequest := lhproto.PutPrincipalRequest{
			Id:            id,
			PerTenantAcls: per_tenant_acls,
			Overwrite:     overwrite,
		}

		littlehorse.PrintResp(getGlobalClient(cmd).PutPrincipal(
			requestContext(cmd),
			&putRequest,
		))
	},
}

var getPrincipalCmd = &cobra.Command{
	Use:   "principal <id>",
	Short: "Get a Principal",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		littlehorse.PrintResp(getGlobalClient(cmd).GetPrincipal(
			requestContext(cmd),
			&lhproto.PrincipalId{
				Id: args[0],
			},
		))
	},
}

var searchPrincipalCmd = &cobra.Command{
	Use:   "principal",
	Short: "Search for Principals",
	Long: `
Search for Principals. You may provide any of the following option groups:

[isAdmin, tenantId]

* Note: '--isAdmin' is a Boolean flag with 3 states:
	- return ALL          Flag is not present
	- return only TRUE    Flag is present or reads '--isAdmin=true'
	- return only FALSE   Flag reads '--isAdmin=false'

* Note: You may optionally use the earliesMinutesAgo and latestMinutesAgo
options with this group to put a time bound on Principals which are returned.
The time bound applies to the time that the Principal was created.

Returns a list of ObjectId's that can be passed into 'lhctl get principals'.
	`,
	Args: cobra.ExactArgs(0),
	Run: func(cmd *cobra.Command, args []string) {
		isAdmin, _ := cmd.Flags().GetBool("isAdmin")
		tenantId, _ := cmd.Flags().GetString("tenantId")
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")

		earliest, latest := loadEarliestAndLatestStart(cmd)

		search := &lhproto.SearchPrincipalRequest{
			Bookmark:      bookmark,
			Limit:         &limit,
			EarliestStart: earliest,
			LatestStart:   latest,
		}

		if cmd.Flags().Lookup("isAdmin").Changed {
			search.PrincipalCriteria = &lhproto.SearchPrincipalRequest_IsAdmin{
				IsAdmin: isAdmin,
			}
		} else if tenantId != "" {
			search.PrincipalCriteria = &lhproto.SearchPrincipalRequest_TenantId{
				TenantId: tenantId,
			}
		}

		littlehorse.PrintResp(getGlobalClient(cmd).SearchPrincipal(requestContext(cmd), search))
	},
}

var deployPrincipalCmd = &cobra.Command{
	Use:   "principal <file>",
	Short: "Deploy Principal from a file",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		putPrincipalReq := &lhproto.PutPrincipalRequest{}

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

		littlehorse.PrintResp(getGlobalClient(cmd).PutPrincipal(requestContext(cmd), putPrincipalReq))
	},
}

func parseAcl(input string) map[lhproto.ACLResource][]lhproto.ACLAction {
	// Split the input string into individual permissions
	permissions := strings.Split(input, ",")

	entityMap := make(map[lhproto.ACLResource][]lhproto.ACLAction)

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
				entityMap[entity] = []lhproto.ACLAction{access}
			}
		}
	}
	return entityMap
}

var (
	actionsMap = map[string]lhproto.ACLAction{
		"read":  lhproto.ACLAction_READ,
		"run":   lhproto.ACLAction_RUN,
		"write": lhproto.ACLAction_WRITE_METADATA,
		"all":   lhproto.ACLAction_ALL_ACTIONS,
	}
	entitiesMap = map[string]lhproto.ACLResource{
		"acl_workflow":       lhproto.ACLResource_ACL_WORKFLOW,
		"acl_task":           lhproto.ACLResource_ACL_TASK,
		"acl_external_event": lhproto.ACLResource_ACL_EXTERNAL_EVENT,
		"acl_user_task":      lhproto.ACLResource_ACL_USER_TASK,
		"acl_principal":      lhproto.ACLResource_ACL_PRINCIPAL,
		"acl_tenant":         lhproto.ACLResource_ACL_TENANT,
		"all":                lhproto.ACLResource_ACL_ALL_RESOURCES,
	}
)

var deletePrincipalCmd = &cobra.Command{
	Use:   "principal <id>",
	Short: "Delete a Principal.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		littlehorse.PrintResp(getGlobalClient(cmd).DeletePrincipal(
			requestContext(cmd),
			&lhproto.DeletePrincipalRequest{
				Id: &lhproto.PrincipalId{
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

	searchCmd.AddCommand(searchPrincipalCmd)
	searchPrincipalCmd.Flags().String("tenantId", "", "List Principals associated with this Tenant ID")
	searchPrincipalCmd.Flags().Bool("isAdmin", false, "List only Principals that are admins")
	searchPrincipalCmd.Flags().Int("earliestMinutesAgo", -1, "Search only for Principals that were created no more than this number of minutes ago")
	searchPrincipalCmd.Flags().Int("latestMinutesAgo", -1, "Search only for Principals that were created at least this number of minutes ago")

	getCmd.AddCommand(getPrincipalCmd)

	deployCmd.AddCommand(deployPrincipalCmd)
	deleteCmd.AddCommand(deletePrincipalCmd)
}
