package cmd

import (
	"fmt"
	"log"

	emptypb "github.com/golang/protobuf/ptypes/empty"
	"github.com/spf13/cobra"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

var versionCmd = &cobra.Command{
	Use:   "version",
	Short: "Print Client and Server Version Information.",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("lhctl version: 0.7.2")

		resp, err := getGlobalClient(cmd).GetServerVersion(requestContext(), &emptypb.Empty{})
		if err != nil {
			if grpcStatus, ok := status.FromError(err); ok && grpcStatus.Code() == codes.Unimplemented {
				fmt.Println("Server is outdated")
			} else {
				log.Fatal(err)
			}
		} else {
			serverVersion := fmt.Sprintf("%d.%d.%d", resp.MajorVersion, resp.MinorVersion, resp.PatchVersion)

			if resp.PreReleaseIdentifier != nil {
				serverVersion = serverVersion + "-" + *resp.PreReleaseIdentifier
			}

			fmt.Println("Server version: " + serverVersion)
		}
	},
}

func init() {
	rootCmd.AddCommand(versionCmd)
}
