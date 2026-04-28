# Gradle Build Cache

**Source:** https://docs.gradle.org/current/userguide/build_cache.html  
**Date d'extraction:** 2025-01-17

---

## Overview

The Gradle build cache is a cache mechanism that aims to save time by reusing outputs produced by other builds. The build cache works by storing (locally or remotely) build outputs and allowing builds to fetch these outputs from the cache when it is determined that inputs have not changed, avoiding the expensive work of regenerating them.

A first feature using the build cache is task output caching. Essentially, task output caching leverages the same intelligence as up-to-date checks that Gradle uses to avoid work when a previous local build has already produced a set of task outputs. But instead of being limited to the previous build in the same workspace, task output caching allows Gradle to reuse task outputs from any earlier build in any location on the local machine. When using a shared build cache for task output caching this even works across developer machines and build agents.

Apart from tasks, artifact transforms can also leverage the build cache and re-use their outputs similarly to task output caching.

## Enable the Build Cache

By default, the build cache is not enabled. You can enable the build cache in a couple of ways:

**Run with --build-cache on the command-line**

Gradle will use the build cache for this build only.

**Put org.gradle.caching=true in your gradle.properties**

Gradle will try to reuse outputs from previous builds for all builds, unless explicitly disabled with --no-build-cache.

When the build cache is enabled, it will store build outputs in the Gradle User Home.

## Task Output Caching

Beyond incremental builds described in up-to-date checks, Gradle can save time by reusing outputs from previous executions of a task by matching inputs to the task. Task outputs can be reused between builds on one computer or even between builds running on different computers via a build cache.

We have focused on the use case where users have an organization-wide remote build cache that is populated regularly by continuous integration builds. Developers and other continuous integration agents should load cache entries from the remote build cache. We expect that developers will not be allowed to populate the remote build cache, and all continuous integration builds populate the build cache after running the clean task.

For your build to play well with task output caching it must work well with the incremental build feature. For example, when running your build twice in a row all tasks with outputs should be UP-TO-DATE. You cannot expect faster builds or correct builds when enabling task output caching when this prerequisite is not met.

Task output caching is automatically enabled when you enable the build cache.

## What does it look like

Let us start with a project using the Java plugin which has a few Java source files. We run the build the first time.

```console
> gradle --build-cache compileJava
:compileJava
:processResources
:classes
:jar
:assemble

BUILD SUCCESSFUL
```

We see the directory used by the local build cache in the output. Apart from that the build was the same as without the build cache. Let's clean and run the build again.

```console
> gradle clean
:clean

BUILD SUCCESSFUL
```

```console
> gradle --build-cache assemble
:compileJava FROM-CACHE
:processResources
:classes
:jar
:assemble

BUILD SUCCESSFUL
```

Now we see that, instead of executing the :compileJava task, the outputs of the task have been loaded from the build cache. The other tasks have not been loaded from the build cache since they are not cacheable. This is due to :classes and :assemble being lifecycle tasks and :processResources and :jar being Copy-like tasks which are not cacheable since it is generally faster to execute them.

## Cacheable tasks

Since a task describes all of its inputs and outputs, Gradle can compute a build cache key that uniquely defines the task's outputs based on its inputs. That build cache key is used to request previous outputs from a build cache or store new outputs in the build cache. If the previous build outputs have been already stored in the cache by someone else, e.g. your continuous integration server or other developers, you can avoid executing most tasks locally.

The following inputs contribute to the build cache key for a task in the same way that they do for up-to-date checks:

- The task type and its classpath
- The names of the output properties
- The names and values of properties annotated as described in the section called "Custom task types"
- The names and values of properties added by the DSL via TaskInputs
- The classpath of the Gradle distribution, buildSrc and plugins
- The content of the build script when it affects execution of the task

Task types need to opt-in to task output caching using the @CacheableTask annotation. Note that @CacheableTask is not inherited by subclasses. Custom task types are not cacheable by default.

## Built-in cacheable tasks

Currently, the following built-in Gradle tasks are cacheable:

- **Java toolchain:** JavaCompile, Javadoc
- **Groovy toolchain:** GroovyCompile, Groovydoc
- **Scala toolchain:** ScalaCompile, org.gradle.language.scala.tasks.PlatformScalaCompile (removed), ScalaDoc
- **Native toolchain:** CppCompile, CCompile, SwiftCompile
- **Testing:** Test
- **Code quality tasks:** Checkstyle, CodeNarc, Pmd
- **JaCoCo:** JacocoReport
- **Other tasks:** AntlrTask, ValidatePlugins, WriteProperties

All other built-in tasks are currently not cacheable.

Some tasks, like Copy or Jar, usually do not make sense to make cacheable because Gradle is only copying files from one location to another. It also doesn't make sense to make tasks cacheable that do not produce outputs or have no task actions.

## Configure the Build Cache

You can configure the build cache by using the Settings.buildCache(org.gradle.api.Action) block in settings.gradle.

Gradle supports a local and a remote build cache that can be configured separately. When both build caches are enabled, Gradle tries to load build outputs from the local build cache first, and then tries the remote build cache if no build outputs are found. If outputs are found in the remote cache, they are also stored in the local cache, so next time they will be found locally. Gradle stores ("pushes") build outputs in any build cache that is enabled and has BuildCache.isPush() set to true.

By default, the local build cache has push enabled, and the remote build cache has push disabled.

The local build cache is pre-configured to be a DirectoryBuildCache and enabled by default. The remote build cache can be configured by specifying the type of build cache to connect to (BuildCacheConfiguration.remote(java.lang.Class)).

## Built-in local build cache

The built-in local build cache, DirectoryBuildCache, uses a directory to store build cache artifacts. By default, this directory resides in the Gradle User Home, but its location is configurable.

Example configuration:

```groovy
buildCache {
    local {
        directory = new File(rootDir, 'build-cache')
    }
}
```

Gradle will periodically clean-up the local cache directory by removing entries that have not been used recently to conserve disk space.

## Remote HTTP build cache

HttpBuildCache provides the ability read to and write from a remote cache via HTTP.

With the following configuration, the local build cache will be used for storing build outputs while the local and the remote build cache will be used for retrieving build outputs.

```groovy
buildCache {
    remote(HttpBuildCache) {
        url = 'https://example.com:8123/cache/'
    }
}
```

When attempting to load an entry, a GET request is made to https://example.com:8123/cache/«cache-key». The response must have a 2xx status and the cache entry as the body, or a 404 Not Found status if the entry does not exist.

When attempting to store an entry, a PUT request is made to https://example.com:8123/cache/«cache-key». Any 2xx response status is interpreted as success. A 413 Payload Too Large response may be returned to indicate that the payload is larger than the server will accept, which will not be treated as an error.

## Specifying access credentials

HTTP Basic Authentication is supported, with credentials being sent preemptively.

```groovy
buildCache {
    remote(HttpBuildCache) {
        url = 'https://example.com:8123/cache/'
        credentials {
            username = 'build-cache-user'
            password = 'some-complicated-password'
        }
    }
}
```

## Configuration use cases

The recommended use case for the remote build cache is that your continuous integration server populates it from clean builds while developers only load from it. The configuration would then look as follows.

```groovy
boolean isCiServer = System.getenv().containsKey("CI")

buildCache {
    remote(HttpBuildCache) {
        url = 'https://example.com:8123/cache/'
        push = isCiServer
    }
}
```

It is also possible to configure the build cache from an init script, which can be used from the command line, added to your Gradle User Home or be a part of your custom Gradle distribution.

```groovy
gradle.settingsEvaluated { settings ->
    settings.buildCache {
        remote(HttpBuildCache) {
            url = 'https://example.com:8123/cache/'
        }
    }
}
```

## Build cache, composite builds and buildSrc

Gradle's composite build feature allows including other complete Gradle builds into another. Such included builds will inherit the build cache configuration from the top level build, regardless of whether the included builds define build cache configuration themselves or not.

The build cache configuration present for any included build is effectively ignored, in favour of the top level build's configuration. This also applies to any buildSrc projects of any included builds.

The buildSrc directory is treated as an included build, and as such it inherits the build cache configuration from the top-level build.

This configuration precedence does not apply to plugin builds included through pluginManagement as these are loaded before the cache configuration itself.