# LittleHorse CLI

## Installing `lhctl`

1. To install `lhctl` from the source code, first run the following commands within the `/lhctl/` directory:

```bash
go work init
go work use ./../sdk-go
go work use .
```

> [!NOTE]
> This sets up a multi-module workspace in Go that links `sdk-go` to `lhctl`. This is necessary to use `lhctl`, as it depends on the `sdk-go` implementation of the LittleHorse client. Alternatively,  `lhctl` is **not** bundled alongside official releases of `sdk-go`.

2. Run the following command within the `/lhctl/` directory to update your local installation of `lhctl` whenever you make changes.

```
go install .
```

> Make sure it's on the path <br />
> `export GOPATH="$(go env GOPATH)"` <br />
> `export PATH="$PATH:$GOPATH/bin"`

Verify the installation:

```
lhctl
```

## Writing `lhctl` commands

To ensure consistency across our CLI commands, we adhere to the following standards, inspired by [Docopt](http://docopt.org) and the [Cobra User Guide](https://github.com/spf13/cobra/blob/main/site/content/user_guide.md). 

### Arguments

1. Arguments are validated using the `Args` field of the Cobra command

Example:
```go
var putTenantCmd = &cobra.Command{
	Use:   "tenant <id>",
	Short: "Create a Tenant. Currently, updating Tenants is not supported.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
      // No argument validation here
   }
}
```
2. Required arguments are surrounded by `<` and `>`
   1. ex: `lhctl get princial <id>`
3. Optional arguments are surrounded by `[` and `]`
   1. ex: `lhctl search externalEvent [<externalEventDefName>]`
4. Parentheses `(` and `)` also denote required elements
   1. Useful when two arguments are optional in total, but required together
   2. ex: `lhctl run <wfSpecName> [(<var1 name> <var1 val>)]`
5. Ellipsis `...` show that the nearest element to the left can be repeated
   1. ex: `lhctl run <wfSpecName> [(<var1 name> <var1 val>)]...`
For more examples of argument validation functions, including using custom validators, see the [Cobra User Guide](https://github.com/spf13/cobra/blob/main/site/content/user_guide.md#positional-and-custom-arguments).

### Flags

1. Flags modify the behavior of the underlying command, like filtering a search. 
2. Flags are set using the Flag modifiers built into Cobra commands

Example:
```go
putPrincipalCmd.Flags().String("acl", "", "ACLs")
putPrincipalCmd.Flags().Bool("overwrite", false, "Overwrites principal information")
putPrincipalCmd.Flags().String("tenantId", "", "Tenant associated with the principal")
```

For more examples of flag modifiers, see the [Cobra User Guide](https://github.com/spf13/cobra/blob/main/site/content/user_guide.md#working-with-flags).

3. Boolean flags in Search commands have 3 states:

| State           | What the search returns      |
|-----------------|------------------------------|
| Not present     | Both true and false          |
| Present, true   | This field is true           |
| Present, false  | This field is false          |

To accomplish this, we can check if a flag is set by the user. If the flag is not set, we do not pass any value on with the request.

Example from the `lhctl search externalEvent` command:
```go
if cmd.Flags().Lookup("isClaimed").Changed {
   // Flag has been changed by the user
   search.IsClaimed = &isClaimed
}
```
