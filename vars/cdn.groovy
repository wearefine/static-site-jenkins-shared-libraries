#!/usr/bin/env groovy

def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  if (!config.DEBUG){
    env.DEBUG = 'false'
  }
  if (!config.SLACK_CHANNEL){
    env.SLACK_CHANNEL = '#deploys'
  }
   if (!config.DOCKER_REGISTRY_CREDS_ID) {
    error 'DOCKER_REGISTRY_CREDS_ID is required to use Docker builds'
  }
  if (!config.DOCKER_REGISTRY_URL){
    error 'DOCKER_REGISTRY_URL is required to use Docker builds'
  }
  if (!config.AWS_DEFAULT_REGION){
    env.AWS_DEFAULT_REGION = 'us-west-2'
  } else {
    env.AWS_DEFAULT_REGION = config.AWS_DEFAULT_REGION
  }
  if (!config.AWS_CREDS_ID){
    error 'AWS_CREDS_ID is required'
  }
  if (!config.PROD_BUCKET){
    error 'PROD_BUCKET is required'
  }
  if (!config.DEV_BUCKET){
    error 'DEV_BUCKET is required'
  }
  if (!config.DISTRIBUTION){
    error 'DISTRIBUTION is required'
  }
  if (!config.NODE_MAJOR_VERSION){
    error 'NODE_MAJOR_VERSION is required to use Docker builds'
  }

  node {
    timestamps {
      if (env.DEBUG == 'false') {
        notifySlack(env.SLACK_CHANNEL)
      }

      try {
        stage('Checkout') {
          checkout scm
          currentBuild.result = 'SUCCESS'
        }
      } catch(Exception e) {
        currentBuild.result = 'FAILURE'
        if (env.DEBUG == 'false') {
          notifySlack(env.SLACK_CHANNEL)
        }
        throw e
      }

      docker.withRegistry(config.DOCKER_REGISTRY_URL, "ecr:${env.AWS_DEFAULT_REGION}:${config.DOCKER_REGISTRY_CREDS_ID}") {

        docker.image("${config.DOCKER_REGISTRY}:${config.NODE_MAJOR_VERSION}-dev").inside(containerArgs) {
          try {
            stage('Install Dependencies'){
              milestone label: 'Install Dependencies'

              retry(2) {
                sh 'yarn --no-progress --non-interactive --frozen-lockfile'
              }

              currentBuild.result = 'SUCCESS'

            }
          } catch(Exception e) {
            currentBuild.result = 'FAILURE'
            notifySlack(env.SLACK_CHANNEL)
            throw e
          }

          try {
            stage('Deploy'){
              milestone label: 'Deploy'

              withAWS(credentials: config.AWS_CREDS_ID, region: config.AWS_DEFAULT_REGION) {
                if (env.BRANCH_NAME == 'master') {
                  s3Upload(file: 'dist', bucket: env.PROD_BUCKET, path: "custom/")
                  cfInvalidate(distribution: env.DISTRIBUTION, paths:['/custom/*'])
                }
                else if (env.BRANCH_NAME == 'dev') {
                  s3Upload(file: 'dist', bucket: env.DEV_BUCKET, path: "/custom/")
                }
              }
            }
            currentBuild.result = 'SUCCESS'
          } catch(Exception e) {
            currentBuild.result = 'FAILURE'
            if (env.DEBUG == 'false') {
              notifySlack(env.SLACK_CHANNEL)
            }
            throw e
          }
        } // dockerImage
      } // withRegistry
    if (env.DEBUG == 'false') {
      notifySlack(env.SLACK_CHANNEL)
    }
    } // timestamps
  } // node
}
