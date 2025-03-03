# Releasing

To release a new version of the library and plugin, please follow these steps:

1. Bump Version
   1. Bump `VERSION_NAME` in [gradle.properties](gradle.properties) following semvar semantics
   2. Bump `DANGER_KOTLIN_VERSION` in [Dockerfile](Dockerfile)
   3. Bump `ghcr.io/r0adkll/danger-kotlin` verison in [github-action/Dockerfile](github-action/Dockerfile)
   4. Bump `com.r0adkll.danger:danger-plugin-installer:` version in [danger-kotlin-sample-plugin](danger-kotlin-sample-plugin/build.gradle)
   5. Bump `VERSION` in [scripts/install.sh](scripts/install.sh)
2. Update [CHANGELOG.md](CHANGELOG.md) and [intellij-plugin/CHANGELOG.md](intellij-plugin/CHANGELOG.md)
3. Push to `main`
4. Create release on GitHub
