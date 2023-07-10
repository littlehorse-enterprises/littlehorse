# LittleHorse CLI

To install `lhctl`, simply run:

```
go mod tidy
go install .
```

## Local Dev

To reflect changes from your local `lhctl` in this branch, you should:

```
go mod edit -replace bitbucket.org/littlehorse-core/littlehorse/sdk-go=../lh-golib
go get bitbucket.org/littlehorse-core/littlehorse/sdk-go@latest
```

**IMPORTANT:** Do NOT check in the `go.mod` pointing to your local file. First, commit your changes to the `lh-golib` repo and push a release (by pushing a tag with a new version). Then remove the `replace` section in your `go.mod` file and run:

```
# FIRST: un-replace your go.mod
echo go.mod | grep -v 'replace bitbucket.org/littlehorse-core' > /tmp/go.mod
mv /tmp/go.mod go.mod

# Update to the latest version of lh-golib
go get bitbucket.org/littlehorse-core/littlehorse/sdk-go@<that tag you just pushed>
go mod tidy
```
