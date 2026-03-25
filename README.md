# PathOps Control Plane

The PathOps Control Plane is the central backend service of the PathOps platform.

It provides the public and administrative APIs used by the CLI and UI, manages tenants and applications, enforces security and policies, and orchestrates integrations with external tools such as Keycloak, Jenkins, Kubernetes, Git providers, and artifact registries.

The Control Plane acts as the source of truth for PathOps state and is responsible for coordinating workflows, provisioning resources, and ensuring that all changes follow the PathOps guardrails model.
