GitHub Actions deployment setup

This repository already contains the workflow at `.github/workflows/elastic-beanstalk.yml`.
It deploys this app to AWS Elastic Beanstalk after every push to `main`.

Use these exact repository variables in GitHub:
- `AWS_REGION=ap-south-1`
- `EB_APPLICATION_NAME=dynamic-rbac`
- `EB_ENVIRONMENT_NAME=dynamic-rbac-prod`
- `EB_S3_BUCKET=elasticbeanstalk-ap-south-1-003107248112`

Use this repository secret in GitHub:
- `AWS_ROLE_ARN=<IAM role ARN assumed by GitHub Actions>`

GitHub repo location:
- Repository: `Harish-Palaspagar/rbac-engine`
- Branch allowed to deploy: `main`

AWS setup required:
1. Create or reuse an S3 bucket for Elastic Beanstalk application bundles.
2. Create an IAM role for GitHub Actions that trusts:
   - OIDC provider `token.actions.githubusercontent.com`
   - GitHub repository `Harish-Palaspagar/rbac-engine`
   - branch `main`
3. Attach permissions that allow:
   - `elasticbeanstalk:CreateApplicationVersion`
   - `elasticbeanstalk:UpdateEnvironment`
   - `elasticbeanstalk:DescribeApplications`
   - `elasticbeanstalk:DescribeApplicationVersions`
   - `elasticbeanstalk:DescribeEnvironments`
   - `elasticbeanstalk:DescribeEvents`
   - `s3:PutObject`, `s3:GetObject`, `s3:AbortMultipartUpload` on `elasticbeanstalk-ap-south-1-003107248112/*`
   - `s3:ListBucket` on `elasticbeanstalk-ap-south-1-003107248112`
4. Save the created role ARN as the GitHub secret `AWS_ROLE_ARN`.

Elastic Beanstalk environment properties for `dynamic-rbac-prod`:
- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL=jdbc:postgresql://<dynamic-rbac-db-endpoint>:5432/<db-name>`
- `DB_USERNAME=<db-user>`
- `DB_PASSWORD=<db-password>`
- `APP_SEED_ENABLED=false`

Runtime notes:
- The deployment bundle contains `application.jar` and `Procfile`
- `Procfile` starts Spring Boot with the `prod` profile on Elastic Beanstalk
- `application-prod.properties` expects `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`

Workflow behavior:
- Pull requests to `main` run build and test only
- Pushes to `main` build, package, upload the ZIP to S3, create an Elastic Beanstalk application version, and deploy it to `dynamic-rbac-prod`
