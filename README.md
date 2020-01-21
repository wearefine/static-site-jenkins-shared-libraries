# static-site-jenkins-shared-libraries

Putting CI/CD on your static site on every change should be a smooth process. With Jenkins pipelines you can describe the entire process through code. We did the hard work to make our static site deployment library open source so you can benefit. It gives you a drop in pipeline shared library with a configurable Jenkinsfile.

## Prerequisites

### Jenkins

If you're new to Jenkins pipelines you should go read the [documentation](https://jenkins.io/doc/book/pipeline/) before proceeding to get a sense for what to expect using this code. The rest of the setup process will assume you have basic knowledge of Jenkins or CI/CD jobs in general.

#### OS

- rvm installed in the jenkins user
- git
- build-essential

#### Jenkins

- Version: > 2.7.3 - tested on (2.150.2 LTS)

#### Plugins

- slack
- pipeline (workflow-aggregator)
- git
- timestamper
- credentials
- docker
- AWS ECR credentials

#### Scripts Approval

When the job runs the first time you will need to work through allowing certain functions to execute in the groovy sandbox. This is normal as not all high use groovy functions are in the default safelist but more are added all the time.

## Jenkinsfile

``` groovy
static {
  NODE_INSTALL_NAME = 'lts/boron'
  SLACK_CHANNEL = '#deploys'
  DEBUG = 'false'
  DOCKER_REGISTRY_CREDS_ID = 'access_docker_hub'
  DOCKER_REGISTRY_URL = 'https://hub.docker.io'
  AWS_DEFAULT_REGION = 'us-east-1'
  AWS_CREDS_ID  = 'aws_creds_id'
  PROD_BUCKET = 'bucket-name'
  DEV_BUCKET = 'bucket-name-dev'
  DISTRIBUTION = '123ABC'
  NODE_MAJOR_VERSION = '12'
}
```

### Required Parameters

- **NODE_INSTALL_NAME:** Nodejs plugin uses names to identify installs in Jenkins, enter that value here [String]
- **DOCKER_REGISTRY_URL:** The private Docker registry URL. Required to build with Docker. [String]
- **DOCKER_REGISTRY_CREDS_ID:** The private Docker registry credentials ID in Jenkins. Required to build with Docker. [String]
- **AWS_DEFAULT_REGION:** The AWS region of you Elastic Container Registry [String]
- **AWS_CRED_ID:** The AWS credentials ID from Jenkins to push files to AWS S3 [String]
- **PROD_BUCKET:** The AWS S3 bucket to deploy production assets into [String]
- **DEV_BUCKET:** The AWS S3 bucket to deploy development assets into [String]
- **DISTRIBUTION:** The Cloudfront distribution to invalidate on deploy [String]
- **NODE_MAJOR_VERSION:** The major version of Nodejs to use for the build container [String]

**Note:** The current setup only works with AWS ECR but can easily be adapted to work with other private registries.

## [MIT License](LICENSE)
