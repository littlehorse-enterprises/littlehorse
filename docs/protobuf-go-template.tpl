# LittleHorse API Reference

The LittleHorse Server exposes a GRPC API to its clients. For most general usage of LittleHorse, you _will not_ need to
read the raw protobuf schema. Common LittleHorse client actions fall into three general categories:

1. Developing a Task Worker, which is handled by our [Task Worker SDK's](/docs/developer-guide/task-worker-development).
2. Developing a WfSpec, which is handled by our [`WfSpec` SDK's](/docs/developer-guide/wfspec-development).
3. Running and interacting with `WfRun`'s, which is documented in our ["Using the API"](/docs/developer-guide/grpc) docs.

However, the highly curious reader might want to see the actual GRPC and Protobuf specification. The docs on this page
are autogenerated from our actual [protobuf files](https://github.com/littlehorse-enterprises/littlehorse/tree/master/schemas).

The documentation of what the specific protobuf fields mean is potentially useful for advanced Jedi Master use-cases.

Happy Reading!

## LittleHorse GRPC API

The LittleHorse GRPC API is the backbone of the clients that you get in all of our SDK's. Every `LHConfig` object
gives you a GRPC stub to access the API. Most common operations are already documented with code examples in different
languages [here](/docs/developer-guide/grpc), but we put this here for the true Jedi Masters.

{{range .Files}}
{{if .HasServices}}
{{range .Services -}}
{{range .Methods -}}
### RPC `{{.Name}}` {#{{.Name | lower | replace "." ""}}}

| Request Type | Response Type | Description |
| ------------ | ------------- | ------------|
| [{{.RequestLongType}}](#{{.RequestLongType | lower | replace "." ""}}) | [{{.ResponseLongType}}](#{{.ResponseLongType | lower | replace "." ""}}) | {{.Description | replace "\n\n" "<br/><br/>" | replace "\n" " "}} |

{{end}}
{{end}}
{{end}}
{{end}}

## LittleHorse Protobuf Schemas

This section contains the exact schemas for every object in our public API.

{{range .Files}}
{{range .Messages}}

### Message `{{.LongName}}` {#{{.LongName | lower | replace "." ""}}}

{{.Description}}

{{if .HasFields}}
| Field | Label | Type | Description |
| ----- | ----  | ---- | ----------- |
{{range .Fields -}}
| `{{.Name}}` | {{if and .IsOneof (ne (printf "%s" .Name) (printf "%s" (slice .OneofDecl 1)))}}oneof `{{.OneofDecl}}`{{else}}{{if .IsMap}}map{{else}}{{.Label}}{{end}}{{end}}| [{{.LongType}}](#{{.LongType | lower | replace "." ""}}) | {{.Description | replace "\n\n" "<br/><br/>" | replace "\n" " "}} |
{{end}} <!-- end Fields -->
{{end}} <!-- end HasFields -->

{{end}}
{{end}}

## LittleHorse Enums

This section contains the enums defined by the LittleHorse API.

{{range .Files}}
{{range .Enums}}

### Enum {{.LongName}} {#{{.LongName | lower | replace "." ""}}}
{{.Description}}

| Name | Number | Description |
| ---- | ------ | ----------- |
{{range .Values -}}
	| {{.Name}} | {{.Number}} | {{nobr .Description}} |
{{end}}

{{end}} <!-- end Enums -->
{{end}} <!-- end Files -->
