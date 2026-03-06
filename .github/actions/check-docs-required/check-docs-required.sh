#!/bin/bash
set -euo pipefail

# Default values
CHECK_MODE="${CHECK_MODE:-both}"
COMMIT_SHA="${COMMIT_SHA:-}"
GITHUB_EVENT_NAME="${GITHUB_EVENT_NAME:-push}"
GITHUB_EVENT_PATH="${GITHUB_EVENT_PATH:-}"
GITHUB_TOKEN="${GITHUB_TOKEN:-}"
GITHUB_REPOSITORY="${GITHUB_REPOSITORY:-}"

DOCS_REQUIRED="false"
COMMITS=""

# Function to check commit message for docs-required
check_commit_message() {
  local commit="${1:-HEAD}"
  local commit_msg
  commit_msg=$(git log --format="%B" -1 "$commit" 2>/dev/null || echo "")
  
  if echo "$commit_msg" | grep -qi "docs-required: true"; then
    echo "true"
  else
    echo "false"
  fi
}

# Function to find PR number associated with a commit via GitHub API
find_pr_for_commit() {
  local commit="${1:-HEAD}"
  # Resolve to SHA for API (API interprets literal "HEAD" as default branch HEAD)
  local commit_sha
  commit_sha=$(git rev-parse "$commit" 2>/dev/null || echo "")
  [ -n "$commit_sha" ] && commit="$commit_sha"
  
  if [ -z "$GITHUB_TOKEN" ] || [ -z "$GITHUB_REPOSITORY" ]; then
    # Fallback to commit message parsing if no token/repo
    local commit_msg
    commit_msg=$(git log --format="%B" -1 "$commit" 2>/dev/null || echo "")
    local pr_num
    pr_num=$(echo "$commit_msg" | grep -oE '(Merge pull request|PR|#)[[:space:]]*#[0-9]+' | grep -oE '[0-9]+' | head -1)
    echo "${pr_num:-}"
    return
  fi
  
  # Use GitHub API to find PRs associated with this commit
  # This endpoint requires a special Accept header
  local pr_data
  pr_data=$(curl -s \
    -H "Authorization: token $GITHUB_TOKEN" \
    -H "Accept: application/vnd.github.groot-preview+json" \
    "https://api.github.com/repos/$GITHUB_REPOSITORY/commits/$commit/pulls" 2>/dev/null || echo "")
  
  # Check if we got valid data (not empty and not an error)
  if [ -z "$pr_data" ] || echo "$pr_data" | jq -e '.message' >/dev/null 2>&1; then
    # API call failed or returned error, fallback to commit message parsing
    local commit_msg
    commit_msg=$(git log --format="%B" -1 "$commit" 2>/dev/null || echo "")
    local pr_num
    pr_num=$(echo "$commit_msg" | grep -oE '(Merge pull request|PR|#)[[:space:]]*#[0-9]+' | grep -oE '[0-9]+' | head -1)
    echo "${pr_num:-}"
    return
  fi
  
  # Extract the first PR number (usually there's only one)
  local pr_num
  pr_num=$(echo "$pr_data" | jq -r '.[0].number // empty' 2>/dev/null || echo "")
  
  if [ -n "$pr_num" ] && [ "$pr_num" != "null" ] && [ "$pr_num" != "" ]; then
    echo "$pr_num"
  else
    # Fallback: try to extract from commit message if API doesn't return PR
    local commit_msg
    commit_msg=$(git log --format="%B" -1 "$commit" 2>/dev/null || echo "")
    pr_num=$(echo "$commit_msg" | grep -oE '(Merge pull request|PR|#)[[:space:]]*#[0-9]+' | grep -oE '[0-9]+' | head -1)
    echo "${pr_num:-}"
  fi
}

# Function to check PR labels via GitHub API
check_pr_labels_api() {
  local pr_number="$1"
  
  if [ -z "$pr_number" ] || [ -z "$GITHUB_TOKEN" ] || [ -z "$GITHUB_REPOSITORY" ]; then
    echo "false"
    return
  fi
  
  # Prefer gh CLI when available (handles API responses robustly, avoids jq parse issues)
  if command -v gh &>/dev/null; then
    local labels
    labels=$(GH_TOKEN="$GITHUB_TOKEN" gh pr view "$pr_number" --repo "$GITHUB_REPOSITORY" --json labels -q '.labels[].name' 2>/dev/null || echo "")
    if echo "$labels" | grep -qi "docs-required"; then
      echo "true"
      return
    fi
    echo "false"
    return
  fi
  
  # Fallback: curl + jq (write to temp file to avoid control-char issues when piping)
  local tmp
  tmp=$(mktemp)
  trap "rm -f $tmp" RETURN
  curl -s \
    -H "Authorization: token $GITHUB_TOKEN" \
    -H "Accept: application/vnd.github.v3+json" \
    "https://api.github.com/repos/$GITHUB_REPOSITORY/pulls/$pr_number" -o "$tmp" 2>/dev/null || true
  
  if [ ! -s "$tmp" ]; then
    echo "false"
    return
  fi
  
  local label_count
  label_count=$(jq -r '.labels[]?.name // empty' "$tmp" 2>/dev/null | grep -ci "docs-required" || echo "0")
  label_count=$(printf '%s' "${label_count:-0}" | tr -d '\n\r' | head -1)
  
  if [ "${label_count:-0}" -gt 0 ] 2>/dev/null; then
    echo "true"
  else
    echo "false"
  fi
}

# Function to check PR labels from event
check_pr_labels() {
  if [ "$GITHUB_EVENT_NAME" != "pull_request" ]; then
    echo "false"
    return
  fi
  
  # Check if event file exists and has labels
  if [ -n "$GITHUB_EVENT_PATH" ] && [ -f "$GITHUB_EVENT_PATH" ]; then
    # Check if any label matches "docs-required" (case-insensitive)
    local label_count
    label_count=$(jq -r '.pull_request.labels[]?.name // empty' "$GITHUB_EVENT_PATH" 2>/dev/null | grep -ci "docs-required" || echo "0")
    
    if [ "$label_count" -gt 0 ]; then
      echo "true"
      return
    fi
  fi
  
  echo "false"
}

# Determine which commits to check
if [ "$GITHUB_EVENT_NAME" = "workflow_dispatch" ] && [ -n "$COMMIT_SHA" ]; then
  # Manual dispatch with specific commit
  COMMITS_TO_CHECK="$COMMIT_SHA"
elif [ "$GITHUB_EVENT_NAME" = "pull_request" ]; then
  # For PRs, check the PR head commit
  if [ -n "$GITHUB_EVENT_PATH" ] && [ -f "$GITHUB_EVENT_PATH" ]; then
    PR_SHA=$(jq -r '.pull_request.head.sha // empty' "$GITHUB_EVENT_PATH" 2>/dev/null || echo "")
    if [ -n "$PR_SHA" ] && [ "$PR_SHA" != "null" ]; then
      COMMITS_TO_CHECK="$PR_SHA"
    else
      COMMITS_TO_CHECK="$(git rev-parse HEAD)"
    fi
  else
    COMMITS_TO_CHECK="$(git rev-parse HEAD)"
  fi
else
  # For push events, since we squash commits, just check HEAD
  # Resolve to actual SHA so downstream actions get a real commit hash
  COMMITS_TO_CHECK="$(git rev-parse HEAD)"
fi

# Check based on mode
if [ "$CHECK_MODE" = "label" ] || [ "$CHECK_MODE" = "both" ]; then
  # For PR events, check labels from event
  if [ "$GITHUB_EVENT_NAME" = "pull_request" ]; then
    if [ "$(check_pr_labels)" = "true" ]; then
      DOCS_REQUIRED="true"
      if [ -z "$COMMITS" ]; then
        COMMITS="${COMMITS_TO_CHECK%% *}"  # Take first commit
      fi
    fi
  # For push events, determine if it's a PR merge or direct push
  elif [ "$GITHUB_EVENT_NAME" = "push" ] || [ "$GITHUB_EVENT_NAME" = "workflow_dispatch" ]; then
    for COMMIT in $COMMITS_TO_CHECK; do
      # Try to find if this commit came from a PR (squash merge)
      PR_NUM=$(find_pr_for_commit "$COMMIT")
      
      if [ -n "$PR_NUM" ]; then
        # Case 1: Squash commit from PR - check PR label
        echo "Found PR #$PR_NUM for commit $COMMIT (squash merge)"
        if [ "$(check_pr_labels_api "$PR_NUM")" = "true" ]; then
          echo "PR #$PR_NUM has docs-required label"
          DOCS_REQUIRED="true"
          if [ -z "$COMMITS" ]; then
            COMMITS="$COMMIT"
          else
            if [[ ! " $COMMITS " =~ " $COMMIT " ]]; then
              COMMITS="$COMMITS $COMMIT"
            fi
          fi
        else
          echo "PR #$PR_NUM does not have docs-required label"
          # Fallback: squash merge may include docs-required: true from squashed commits
          if [ "$(check_commit_message "$COMMIT")" = "true" ]; then
            echo "Commit $COMMIT has docs-required: true in squash message"
            DOCS_REQUIRED="true"
            if [ -z "$COMMITS" ]; then
              COMMITS="$COMMIT"
            else
              if [[ ! " $COMMITS " =~ " $COMMIT " ]]; then
                COMMITS="$COMMITS $COMMIT"
              fi
            fi
          fi
        fi
      else
        # Case 2: Direct push to master - check commit message footer
        echo "No PR found for commit $COMMIT (direct push)"
        if [ "$(check_commit_message "$COMMIT")" = "true" ]; then
          echo "Commit $COMMIT has docs-required: true in footer"
          DOCS_REQUIRED="true"
          if [ -z "$COMMITS" ]; then
            COMMITS="$COMMIT"
          else
            if [[ ! " $COMMITS " =~ " $COMMIT " ]]; then
              COMMITS="$COMMITS $COMMIT"
            fi
          fi
        fi
      fi
    done
  fi
fi

# Also check commit messages for direct pushes (if not already checked above)
if [ "$CHECK_MODE" = "commit" ] || [ "$CHECK_MODE" = "both" ]; then
  # Only check commit messages if we haven't already processed this commit
  # (for push/workflow_dispatch events, we already checked above)
  if [ "$GITHUB_EVENT_NAME" != "push" ] && [ "$GITHUB_EVENT_NAME" != "workflow_dispatch" ]; then
    for COMMIT in $COMMITS_TO_CHECK; do
      if [ "$(check_commit_message "$COMMIT")" = "true" ]; then
        DOCS_REQUIRED="true"
        if [ -z "$COMMITS" ]; then
          COMMITS="$COMMIT"
        else
          # Avoid duplicates
          if [[ ! " $COMMITS " =~ " $COMMIT " ]]; then
            COMMITS="$COMMITS $COMMIT"
          fi
        fi
      fi
    done
  fi
fi

# Output results
echo "docs_required=$DOCS_REQUIRED" >> "$GITHUB_OUTPUT"
if [ -n "$COMMITS" ]; then
  echo "commits<<EOF" >> "$GITHUB_OUTPUT"
  echo "$COMMITS" >> "$GITHUB_OUTPUT"
  echo "EOF" >> "$GITHUB_OUTPUT"
fi

if [ "$DOCS_REQUIRED" = "true" ]; then
  echo "✅ Found commits requiring documentation: $COMMITS"
else
  echo "ℹ️ No documentation required"
fi
