GitHub Actions deployment setup

Repository variables:
- `AWS_REGION`: AWS region, for example `ap-south-1`
- `EB_APPLICATION_NAME`: Elastic Beanstalk application name, for example `dynamic-rbac`
- `EB_ENVIRONMENT_NAME`: Elastic Beanstalk environment name, for example `dynamic-rbac-prod`
- `EB_S3_BUCKET`: S3 bucket used for Elastic Beanstalk application versions

Repository secrets:
- `AWS_ROLE_ARN`: IAM role ARN that GitHub Actions can assume with OIDC

Recommended IAM role permissions:
- `elasticbeanstalk:CreateApplicationVersion`
- `elasticbeanstalk:UpdateEnvironment`
- `s3:PutObject`
- `s3:GetObject`
- `s3:ListBucket`

Workflow behavior:
- Pull requests to `main` run test and package steps only
- Pushes to `main` run test, package, upload the jar to S3, create an Elastic Beanstalk application version, and deploy it
