# Backend release

Push the release workflow to `main`, then select **Actions > Release backend >
Run workflow**, with `main` selected. The workflow requires permission to push
commits and tags to `main`; branch protection or repository Actions settings
may need to permit this.

The workflow releases the current `MAJOR.MINOR.PATCH-SNAPSHOT` version from
`pom.xml`. It commits and tags the non-SNAPSHOT version as
`version/MAJOR.MINOR.PATCH`, builds and verifies that commit, advances `main`
to the next patch SNAPSHOT, and attaches the release JAR to a GitHub release.
Changes pushed by `GITHUB_TOKEN` do not normally start other push-triggered
workflows.

Download the JAR from the GitHub release and deploy it manually to the server.
The workflow does not modify the server.

If publication fails after the commits and tag are pushed, create the GitHub
release manually from the existing tag and attach a JAR built from that tag.
Do not rerun the workflow to recover that release: `main` has already advanced
to the next version. If an atomic push fails, nothing is pushed; resolve the
reported issue and rerun the workflow.
