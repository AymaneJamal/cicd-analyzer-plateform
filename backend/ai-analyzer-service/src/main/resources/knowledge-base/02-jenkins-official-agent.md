# Jenkins Pipeline Syntax - Agent

**Source:** https://www.jenkins.io/doc/book/pipeline/syntax/#agent  
**Date d'extraction:** 2025-01-17

---

## agent

The `agent` section specifies where the entire Pipeline, or a specific stage, will execute in the Jenkins environment depending on where the `agent` section is placed. The section must be defined at the top-level inside the `pipeline` block, but stage-level usage is optional.

**Required:** Yes

**Parameters:** Described below

**Allowed:** In the top-level `pipeline` block and each `stage` block.

---

## Differences between top level agents and stage level agents

There are some nuances when adding an agent to the top level or a stage level when the `options` directive is applied. Check the section options for more information.

### Top Level Agents

In `agents` declared at the top level of a Pipeline, an agent is allocated and then the `timeout` option is applied. The time to allocate the agent is not included in the limit set by the `timeout` option.

```groovy
pipeline {
    agent any
    options {
        // Timeout counter starts AFTER agent is allocated
        timeout(time: 1, unit: 'SECONDS')
    }
    stages {
        stage('Example') {
            steps {
                echo 'Hello World'
            }
        }
    }
}
```

### Stage Agents

In `agents` declared within a stage, the options are invoked before allocating the `agent` and before checking any `when` conditions. In this case, when using `timeout`, it is applied before the `agent` is allocated. The time to allocate the agent is included in the limit set by the `timeout` option.

```groovy
pipeline {
    agent none
    stages {
        stage('Example') {
            agent any
            options {
                // Timeout counter starts BEFORE agent is allocated
                timeout(time: 1, unit: 'SECONDS')
            }
            steps {
                echo 'Hello World'
            }
        }
    }
}
```

This timeout will include the agent provisioning time. Because the timeout includes the agent provisioning time, the Pipeline may fail in cases where agent allocation is delayed.

---

## Parameters

In order to support the wide variety of use-cases Pipeline authors may have, the agent section supports a few different types of parameters. These parameters can be applied at the top-level of the pipeline block, or within each stage directive.

### any

Execute the Pipeline, or stage, on any available agent. For example: `agent any`

### none

When applied at the top-level of the pipeline block no global agent will be allocated for the entire Pipeline run and each stage section will need to contain its own agent section. For example: `agent none`

### label

Execute the Pipeline, or stage, on an agent available in the Jenkins environment with the provided label. For example: `agent { label 'my-defined-label' }`

Label conditions can also be used: For example: `agent { label 'my-label1 && my-label2' }` or `agent { label 'my-label1 || my-label2' }`

### node

`agent { node { label 'labelName' } }` behaves the same as `agent { label 'labelName' }`, but node allows for additional options (such as customWorkspace).

### docker

Execute the Pipeline, or stage, with the given container which will be dynamically provisioned on a node pre-configured to accept Docker-based Pipelines, or on a node matching the optionally defined label parameter. docker also optionally accepts an args parameter which may contain arguments to pass directly to a docker run invocation, and an alwaysPull option, which will force a docker pull even if the image name is already present. For example: `agent { docker 'maven:3.9.3-eclipse-temurin-17' }` or

```groovy
agent {
    docker {
        image 'maven:3.9.3-eclipse-temurin-17'
        label 'my-defined-label'
        args  '-v /tmp:/tmp'
    }
}
```

docker also optionally accepts a registryUrl and registryCredentialsId parameters which will help to specify the Docker Registry to use and its credentials. The parameter registryCredentialsId could be used alone for private repositories within the docker hub. For example:

```groovy
agent {
    docker {
        image 'myregistry.com/node'
        label 'my-defined-label'
        registryUrl 'https://myregistry.com/'
        registryCredentialsId 'myPredefinedCredentialsInJenkins'
    }
}
```

### dockerfile

Execute the Pipeline, or stage, with a container built from a Dockerfile contained in the source repository. In order to use this option, the Jenkinsfile must be loaded from either a Multibranch Pipeline or a Pipeline from SCM. Conventionally this is the Dockerfile in the root of the source repository: `agent { dockerfile true }`. If building a Dockerfile in another directory, use the dir option: `agent { dockerfile { dir 'someSubDir' } }`. If your Dockerfile has another name, you can specify the file name with the filename option. You can pass additional arguments to the docker build …​ command with the additionalBuildArgs option, like `agent { dockerfile { additionalBuildArgs '--build-arg foo=bar' } }`. For example, a repository with the file build/Dockerfile.build, expecting a build argument version:

```groovy
agent {
    // Equivalent to "docker build -f Dockerfile.build --build-arg version=1.0.2 ./build/
    dockerfile {
        filename 'Dockerfile.build'
        dir 'build'
        label 'my-defined-label'
        additionalBuildArgs  '--build-arg version=1.0.2'
        args '-v /tmp:/tmp'
    }
}
```

dockerfile also optionally accepts a registryUrl and registryCredentialsId parameters which will help to specify the Docker Registry to use and its credentials. For example:

```groovy
agent {
    dockerfile {
        filename 'Dockerfile.build'
        dir 'build'
        label 'my-defined-label'
        registryUrl 'https://myregistry.com/'
        registryCredentialsId 'myPredefinedCredentialsInJenkins'
    }
}
```

### kubernetes

Execute the Pipeline, or stage, inside a pod deployed on a Kubernetes cluster. In order to use this option, the Jenkinsfile must be loaded from either a Multibranch Pipeline or a Pipeline from SCM. The Pod template is defined inside the kubernetes { } block. For example, if you want a pod with a Kaniko container inside it, you would define it as follows:

```groovy
agent {
    kubernetes {
        defaultContainer 'kaniko'
        yaml '''
kind: Pod
spec:
  containers:
  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug
    imagePullPolicy: Always
    command:
    - sleep
    args:
    - 99d
    volumeMounts:
      - name: aws-secret
        mountPath: /root/.aws/
      - name: docker-registry-config
        mountPath: /kaniko/.docker
  volumes:
    - name: aws-secret
      secret:
        secretName: aws-secret
    - name: docker-registry-config
      configMap:
        name: docker-registry-config
'''
   }
}
```

You will need to create a secret aws-secret for Kaniko to be able to authenticate with ECR. This secret should contain the contents of ~/.aws/credentials. The other volume is a ConfigMap which should contain the endpoint of your ECR registry. For example:

```json
{
      "credHelpers": {
        "<your-aws-account-id>.dkr.ecr.eu-central-1.amazonaws.com": "ecr-login"
      }
}
```

Refer to the following example for reference: https://github.com/jenkinsci/kubernetes-plugin/blob/master/examples/kaniko.groovy