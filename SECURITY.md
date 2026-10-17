# Security policy

## Reporting a vulnerability

Please do not open a public issue for a security problem.

Report it privately with the **Report a vulnerability** button in the **Security** tab of this repository, or contact the maintainers directly.

Please include:
- a description of the problem and its impact,
- the steps to reproduce it,
- the affected version or commit.

We will acknowledge the report as soon as possible and keep you informed of the fix.

## Automated checks

- Dependabot monitors the dependencies, the GitHub Actions and the Docker images.
- CodeQL analyses the code.
- Secret scanning blocks commits that contain credentials.
- SonarQube Cloud analyses the quality of the `develop` branch.

## Secrets

Never commit passwords, tokens, or keys. Use environment variables or a local `.env` file, which is ignored by Git.