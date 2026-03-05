#!/bin/bash
set -euo pipefail

COMMITS="${COMMITS:-}"
SOURCE_REPO="${SOURCE_REPO:-}"
SOURCE_REF="${SOURCE_REF:-}"
SOURCE_SHA="${SOURCE_SHA:-}"
LH_SITE_REPO="${LH_SITE_REPO:-littlehorse-enterprises/lh-site}"
GH_TOKEN="${GH_TOKEN:-}"

if [ -z "$GH_TOKEN" ]; then
  echo "⚠️ GitHub token not configured. Cannot dispatch to lh-site."
  echo "💡 Tip: GITHUB_TOKEN should work for same-organization repos. Check permissions."
  exit 1
fi

if [ -z "$COMMITS" ]; then
  echo "⚠️ No commits provided. Nothing to dispatch."
  exit 0
fi

for COMMIT in $COMMITS; do
  echo "Processing commit: $COMMIT"
  
  # Check if an issue already exists for this commit to prevent duplicates
  # Search for issues with the commit SHA in the body
  EXISTING_ISSUES=$(curl -s \
    -H "Authorization: token $GH_TOKEN" \
    -H "Accept: application/vnd.github.v3+json" \
    "https://api.github.com/repos/$LH_SITE_REPO/issues?state=all&labels=documentation&per_page=100" \
    | jq -r --arg commit "$COMMIT" '.[] | select(.body | contains($commit)) | .number' || echo "")
  
  if [ -n "$EXISTING_ISSUES" ]; then
    echo "⚠️ Issue(s) already exist for commit $COMMIT: $EXISTING_ISSUES"
    echo "⏭️ Skipping to prevent duplicates"
    continue
  fi
  
  echo "Dispatching workflow for commit: $COMMIT"
  
  # Get commit details
  COMMIT_MESSAGE=$(git log --format="%B" -1 "$COMMIT" 2>/dev/null || echo "")
  COMMIT_AUTHOR=$(git log --format="%an" -1 "$COMMIT" 2>/dev/null || echo "")
  COMMIT_DATE=$(git log --format="%ai" -1 "$COMMIT" 2>/dev/null || echo "")
  COMMIT_TITLE=$(echo "$COMMIT_MESSAGE" | head -n 1)

  # Resolve commit author's GitHub username via API
  COMMIT_AUTHOR_USERNAME=$(curl -s \
    -H "Authorization: token $GH_TOKEN" \
    -H "Accept: application/vnd.github.v3+json" \
    "https://api.github.com/repos/$SOURCE_REPO/commits/$COMMIT" \
    | jq -r '.author.login // empty')

  if [ -n "$COMMIT_AUTHOR_USERNAME" ]; then
    echo "Resolved commit author GitHub username: $COMMIT_AUTHOR_USERNAME"
  else
    echo "⚠️ Could not resolve GitHub username for commit author '$COMMIT_AUTHOR'"
  fi
  
  # Prepare dispatch payload
  DISPATCH_PAYLOAD=$(jq -n \
    --arg commit "$COMMIT" \
    --arg commit_message "$COMMIT_MESSAGE" \
    --arg commit_author "$COMMIT_AUTHOR" \
    --arg commit_author_username "$COMMIT_AUTHOR_USERNAME" \
    --arg commit_date "$COMMIT_DATE" \
    --arg commit_title "$COMMIT_TITLE" \
    --arg source_repo "$SOURCE_REPO" \
    --arg source_ref "$SOURCE_REF" \
    --arg source_sha "$SOURCE_SHA" \
    '{
      event_type: "documentation-request",
      client_payload: {
        commit: $commit,
        commit_message: $commit_message,
        commit_author: $commit_author,
        commit_author_username: $commit_author_username,
        commit_date: $commit_date,
        commit_title: $commit_title,
        source_repo: $source_repo,
        source_ref: $source_ref,
        source_sha: $source_sha
      }
    }')
  
  # Dispatch workflow
  RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST \
    -H "Authorization: token $GH_TOKEN" \
    -H "Accept: application/vnd.github.v3+json" \
    -H "Content-Type: application/json" \
    "https://api.github.com/repos/$LH_SITE_REPO/dispatches" \
    -d "$DISPATCH_PAYLOAD")
  
  HTTP_CODE=$(echo "$RESPONSE" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
  RESPONSE_BODY=$(echo "$RESPONSE" | sed '/HTTP_CODE:/d')
  
  if [ "$HTTP_CODE" = "204" ]; then
    echo "✅ Successfully dispatched workflow to $LH_SITE_REPO for commit $COMMIT"
  else
    ERROR_MSG=$(echo "$RESPONSE_BODY" | jq -r '.message // "Unknown error"')
    echo "❌ Failed to dispatch workflow (HTTP $HTTP_CODE): $ERROR_MSG"
    echo "Response: $RESPONSE_BODY"
    exit 1
  fi
done
