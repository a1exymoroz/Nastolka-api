# Git workflow

`main` is protected on GitHub: direct pushes are blocked for everyone, including the repo owner. All changes must land via a pull request.

When starting a new feature or fix:
1. Create a new branch off `main` (e.g. `feature/short-description` or `fix/short-description`) — never commit directly to `main`.
2. Commit work on that branch as normal.
3. When ready, push the branch and open a pull request with `gh pr create`. Do not merge it — the user reviews and merges.
4. Every PR must link to the GitHub issue(s) it addresses. Include a closing keyword (e.g. `Closes #12`, `Fixes #7`) in the PR body so the issue auto-closes on merge. If no related issue exists yet, create one first (and add it to the relevant project) before opening the PR.
