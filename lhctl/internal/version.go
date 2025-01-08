package internal

import (
	"fmt"
	"log"

	"github.com/spf13/cobra"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/emptypb"
)

var versionCmd = &cobra.Command{
	Use:   "version",
	Short: "Print Client and Server Version Information.",
	Args:  cobra.ExactArgs(0),
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("lhctl version: " + rootCmd.Version)

		resp, err := getGlobalClient(cmd).GetServerVersion(requestContext(cmd), &emptypb.Empty{})
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

func SetVersionInfo(version, commit, date string) {
	rootCmd.Version = fmt.Sprintf("%s (Git SHA %s)", version, commit)
}

func init() {
	rootCmd.AddCommand(versionCmd)
}
