# Apache Maven Build Cache Extension - Getting Started

**Source:** https://maven.apache.org/extensions/maven-build-cache-extension/  
**Date d'extraction:** 2025-01-17  
**Version:** 1.2.1

---

## Overview

The build-cache extension will be activated only when using Apache Maven from 3.9.x.

To onboard the build-cache extension you need to complete several steps:

- Declare caching extension in your project (either in `pom.xml` or `.mvn/extensions.xml`)
- Add `maven-build-cache-config.xml` cache config in `.mvn/` (optional) to customize the default behavior
- Validate build results and iteratively adjust config to reflect project specifics properly
- Setup remote cache (optional)

## Declaring build cache extension

```xml
<extension>
    <groupId>org.apache.maven.extensions</groupId>
    <artifactId>maven-build-cache-extension</artifactId>
    <version>1.2.1</version>
</extension>
```

either in `pom.xml`'s `<project>/<build>/<extensions>` or in `.mvn/extensions.xml`'s `<extensions>`. Using core extension model (`.mvn/extensions.xml` file) is preferable as it allows better access to maven APIs and could allow more sophisticated optimizations in the future.

## Adding build cache config

Copy template config `maven-build-cache-config.xml` to `.mvn/` directory of your project. To understand the caching machinery, review the config and read the comments. In a typical scenario, you need to:

- Exclude unstable, temporary files or environment-specific files
- Add critical plugins parameters to runtime reconciliation
- Configure precise source code file selectors. Though source code locations are discovered automatically from project and plugin configs, there might be edge cases.
- Configure remote cache (if using the remote cache)

## Adjusting build cache config

After configuring the extension, run a usual command, for example, `mvn package`, and verify the caching engine is activated:

**Check log output** - there should be the cache-related output or initialization error message:

```console
[INFO] Loading cache configuration from <project dir>/.mvn/maven-build-cache-config.xml
```

**Navigate to your local repo directory** - there should be a sibling directory `build-cache` next to the usual local `repository`.

**Find `buildinfo.xml`** in the cache repository for a typical module and review it. Ensure that:

- Expected source code files are present in the build info
- Review all plugins used in the build and add their critical parameters to the reconciliation

Try to find the best working trade-off between fairness and cache efficiency. Adding unnecessary rules and checks could reduce both performance and cache efficiency (hit rate).

## Adding caching CI and remote cache

To leverage the remote cache feature need to provide shared storage. Any technology supported by Maven Resolver will suffice. In the simplest form, it could be any HTTP web server supporting get/put operations.

Working examples:

- Sonatype Nexus Repository (raw repository)
- Artifactory (generic repository)
- Nginx OSS with fs module