# Security Event Management and Threat Prevention — AWS deployment

Purpose: Map the PDF sections "Security Event Logging", "Security Event Monitoring and Notification", and "Programmed Threats Detection and Prevention" to this application's AWS deployment (Firebase authentication, custom Spring Boot authorization backend, App Runner/Fargate, ECR, encrypted RDS). Provide service mappings, recommended configurations, evidence sources, and playbooks.

Audience: IT Security Manager / Incident Responder / Platform Engineer

Prerequisites and context

- Application authentication: Firebase (users must exist in Firebase datastore). Authorization: Spring Boot REST backend using application-level checks.

- Deployment: App Runner and AWS Fargate running container images hosted in Amazon ECR; encrypted Amazon RDS (MySQL) for persistent data; isolated VPC; RDS access limited by IP whitelist; IAM with least privilege; MFA enabled for administrators.

---

## 1. Security Event Logging (what to record, where, and retention)

Goal: Ensure comprehensive, tamper-evident logging of security-relevant events across cloud control plane, network, application, and database layers.

1.1 Control plane & account activity

- AWS CloudTrail
  - Enable organization-wide CloudTrail with multi-region trails. Record management events, and enable data events for S3 and Lambda where relevant.
  - Retention: Send trails to a centralized, access-restricted S3 bucket in the security account with object lock/immutability where required.
  - Evidence: CloudTrail trail configuration, S3 bucket policy, sample events for key operations (CreateUser, AttachRolePolicy, KMS operations).

1.2 Network & VPC activity

- VPC Flow Logs
  - Enable VPC Flow Logs for application subnets and RDS subnets. Send to CloudWatch Logs or S3 for long-term retention.
  - Evidence: Flow log exports, examples showing allowed/blocked traffic to RDS from whitelisted IP addresses.

1.3 Application and authentication logs

- Spring Boot application logs
  - Ensure structured logging (JSON) for the Spring Boot backend. Log authentication decisions (user id, auth source, timestamp, decision), authorization failures, failed/successful requests affecting PHI.
  - Correlate logs with request IDs and CloudWatch Logs streams; enable log grouping by service/task and ensure retention aligned to policy.

- Firebase authentication events
  - Export Firebase Authentication audit logs (sign-in, sign-up, password resets) into GCP or forward events into your SIEM. If not directly exported, ensure application logs record Firebase user IDs and auth timestamps as part of request traces.
  - Evidence: Firebase auth event exports, correlated application logs showing user_id mapping.

1.4 Container & runtime logs

- Container stdout/stderr
  - Capture container logs (App Runner/Fargate) to CloudWatch Logs using structured formats. Store logs in a centralized log group per environment with retention policies.

1.5 Database logging

- Amazon RDS logging
  - Enable general and slow query logging for MySQL, and enable audit plugin if needed. Export logs to CloudWatch Logs and S3 for retention and search.
  - Evidence: RDS parameter group settings, CloudWatch log streams, snapshot of DB audit outputs.

1.6 Key management and encryption events

- AWS KMS CloudTrail events
  - Log KMS usage (Encrypt, Decrypt, GenerateDataKey) in CloudTrail. Ensure key policies and rotation events are recorded and reviewed.

1.7 Logging best-practices and retention

- Centralize logs in a security account or dedicated log-archive S3 bucket. Protect with bucket policies, enforce MFA delete where required, and use S3 Object Lock for immutable retention where regulatory requirements demand.

- Define retention windows per log type (CloudTrail: 7+ years for regulated data; App logs: per policy), and implement lifecycle policies to move older logs to Glacier/Archive.

---

## 2. Security Event Monitoring and Notification (detection & alerting)

Goal: Continuously monitor logs and telemetry to detect and notify on security events and anomalies against defined thresholds.


2.1 Threat detection services (AWS native)

2.1 Threat detection services (AWS native)

- Amazon GuardDuty
  - GuardDuty is a managed threat-detection service that continuously analyzes multiple telemetry sources (CloudTrail management events, VPC Flow Logs, DNS logs and optional S3 data events) to surface malicious or anomalous activity. For this application, enable GuardDuty in all accounts and designate a security account as the central aggregator (via organization delegation). Configure GuardDuty to send high-severity findings to Security Hub and to an EventBridge bus for automated handling.
  - Operational notes: tune GuardDuty sensitivity where false positives are common (for example, CI/CD IP ranges). Create filters for findings that require immediate on-call response (e.g., credential compromise, data exfiltration indicators) and lower-priority findings for continued investigation.
  - Evidence & actions: GuardDuty findings (finding type, resource, severity), related CloudTrail/VPC events, and the timeline of associated API calls or data flows.

- Amazon Inspector
  - Use the new Amazon Inspector (not Classic) to scan container images in ECR (enhanced scanning) and to run host-level assessments when you manage EC2-based build or runtime hosts. Configure Inspector to perform scan-on-push and continuous scans so findings update when new CVEs are announced. Integrate Inspector findings into Security Hub to create consolidated remediation tasks.
  - Operational notes: classify vulnerabilities by severity (CVSS) and map to your patch/rebuild SLAs (for example: critical = 24-48 hours, high = 7 days). Track historical findings to demonstrate remediation timelines to auditors.
  - Evidence & actions: Inspector reports, per-image vulnerability lists, scan timestamps, and CI/CD gating logs showing rejections or promotions.

- AWS Security Hub
  - Security Hub aggregates and normalizes findings from GuardDuty, Inspector, AWS Config, and partner solutions. Use Security Hub insights to create prioritized queues and automated response actions (custom actions that invoke Lambda/SSM). Security Hub also supports standards and frameworks to map findings to compliance requirements.
  - Operational notes: configure integrations (GuardDuty, Inspector, Config) and set up an incident response workflow for high-severity consolidated findings. Use Security Hub to generate evidence exports for audits.



2.2 Custom monitoring (application-level)

- CloudWatch Alarms & Logs Insights
  - Create CloudWatch metrics and alarms for key events: repeated auth failures, sudden increase in error rates, unusual data export volumes, or abnormal request patterns.
  - Use CloudWatch Logs Insights queries to detect anomalies and create alarms that publish to SNS topics.

  Operational detail: Define specific alarm thresholds and anomaly detection models for metrics that indicate potential security incidents. Examples:

  - Repeated auth failures: alarm when failed authentication attempts for a single account exceed X attempts in Y minutes (tune X/Y for your user base). Include rules to auto-block or rate-limit the source IP after threshold.
  - Sudden increase in data egress: create metric filters that count bytes transferred from application endpoints or RDS exports and alert when a spike exceeds baseline + 3σ.
  - Unusual request patterns: use CloudWatch Logs Insights with machine-learning-based anomaly detection (CloudWatch Anomaly Detection) to alert on pattern drift for request rates or error ratios.


2.3 Notification and escalation paths

- EventBridge -> SNS -> Ops channels
  - Route findings and alarms through EventBridge rules to SNS topics, which forward to PagerDuty/Slack/Email or an on-call escalation list.
  - Use separate topics for security-critical and operational alerts to reduce noise.

  Operational detail: Create an EventBridge rule pattern that matches GuardDuty findings with severity >= 7 (High/Critical) and routes them to a `security-critical` SNS topic. The SNS topic should have multiple subscriptions: PagerDuty webhook for immediate paging, Slack channel for analysts, and an S3 ingestion Lambda that archives the finding payload for audit. For lower-severity findings, route to a `security-info` topic which creates a ticket in the ticketing system for review.

  Example EventBridge pattern (conceptual):

  ```json
  {
    "source": ["aws.guardduty"],
    "detail-type": ["GuardDuty Finding"],
    "detail": {"severity": [{"numeric": [7, 10]}]}
  }
  ```


2.4 Automated triage and enrichment

- Lambda enrichment functions
  - On receipt of a GuardDuty/Inspector finding, run a Lambda to enrich context (pull relevant CloudTrail events, related EC2/ECS/Fargate task metadata, ECR image manifest, SBOM) and attach it to the ticket or Security Hub finding.

  Operational detail: The enrichment Lambda should perform the following steps:

  1. Look up the finding resource (for example, the Fargate task ARN, ECR image digest, or IAM user) and fetch recent CloudTrail events for that principal/time window.
  2. Pull VPC Flow Logs and container logs (CloudWatch) for the associated network interfaces and time window to identify data paths.
  3. Retrieve the ECR image manifest, SBOM (if available), and Inspector scan results for the digest.
  4. Aggregate context into a concise document and push it to Security Hub as an attached artifact or to a ticketing system. Include suggested containment actions based on finding type.

  Safety note: the enrichment function must be read-only for investigation steps; containment actions should be taken by a separate, auditable runbook requiring appropriate approvals (see Section 3 playbooks).


2.5 SIEM / central logging platform

- Forward critical logs and enriched findings to a SIEM (Splunk, Sumo Logic, or AWS-native solutions) for correlation across identity, network, application, and endpoint telemetry.

  Operational detail: Normalize logs and findings before ingestion (enriched JSON with standardized fields such as event_time, principal, resource_arn, finding_type, severity, and correlated_event_ids) so that SIEM correlation rules and dashboards can be built easily. Ensure the SIEM receives logs with sufficient retention and access controls to satisfy auditing requirements.

---

## 3. Programmed Threats Detection and Prevention (automated controls and playbooks)

Goal: Combine prevention controls with automated detection and containment to minimize impact and time-to-recovery for programmed threats.



3.1 Preventive controls

- IAM and credential protection
  - Enforce least-privilege IAM by applying the principle of least privilege to roles and policies. Use permission boundaries or service control policies (SCPs) in AWS Organizations to prevent privilege escalation. Require MFA for all console users and consider enforcing hardware MFA for highly privileged accounts. Run regular credential reports and use IAM Access Analyzer to identify roles or policies that allow unintended cross-account access.
  - Operational detail: Prefer short-lived credentials via STS (for example, assume-role with MFA) and use AWS SSO or federated identity where possible to centralize authentication and session control. Rotate service account keys regularly and store secrets in AWS Secrets Manager with rotation enabled for supported engines.

- ECR/Image pipeline hygiene
  - Enforce ECR image scanning on push and integrate scan results into the CI/CD pipeline: images with critical or high severity vulnerabilities should be blocked from promotion to staging/production. Implement image signing and attestation (Sigstore/cosign or AWS Code Signing) and require signature verification as part of the deployment step. Use ECR lifecycle policies to remove old/unused images and enforce immutability for production-tagged images (deploy by digest only).
  - Operational detail: Configure EventBridge rules on ECR image push and Inspector findings to automatically trigger a pipeline job that rebuilds the image with updated base layers or dependency patches, and then re-run scans prior to promotion.

- Network segmentation
  - Architect network boundaries to minimize blast radius: keep application containers in private subnets, place RDS in isolated subnets without public access, and use security groups with least-privilege rules (only open necessary ports to specific CIDR/IPs). Consider using AWS PrivateLink, VPC Endpoints, and AWS Network Firewall for granular control and inspection of east-west traffic. Limit outbound internet access for application tasks where possible.
  - Operational detail: Use Security Groups as the first line of defense and supplement with NACLs and Network Firewall rules for signature-based blocking. Record and periodically review security group rules and use AWS Config rules to detect overly permissive network settings.



3.2 Detection and prevention automation

- EventBridge containment rules
  - Define EventBridge rules that match high-severity findings (for example, GuardDuty 'UnauthorizedAccess:IAMUser/InstanceCredentialExfiltration' or Inspector critical CVEs in production images). Those rules should invoke an orchestration workflow (Lambda or Step Functions) that executes a documented containment playbook. Containment actions should be scoped narrowly and include a human-approval step for destructive actions where necessary.
  - Operational detail: Example containment steps include: remove an IAM role's inline policies, revoke active session tokens using `aws sts revoke-session` or by invalidating sessions in your identity provider, update a task's security group to block outbound traffic, or place an ECR repository into read-only mode by adjusting repository policies.

- Automated patching and rebuilds
  - Integrate vulnerability signals into the CI/CD pipeline: when Inspector or ECR reports a critical vulnerability in a base image, automatically open a ticket and trigger a pipeline job that rebuilds images using an updated base image and dependency set. Block promotion of unrepaired images using pipeline gates.
  - Operational detail: Use EventBridge to trigger CodeBuild/CodePipeline jobs on Inspector findings or image-scan events; generate notifications and require an approval step for production pushes. Maintain an audit trail for rebuilds and promotions to satisfy compliance reviews.



3.3 Programmed threat playbooks (sample)

- Unauthorized access via stolen credentials
  - Detection: A high-severity GuardDuty finding (for example, an unusual console sign-in or API calls from a novel IP range), paired with CloudTrail showing an unusual sequence of administrative API calls, should escalate immediately. Also watch for anomalous spikes in failed authentication attempts or sudden changes in IAM policy attachments.
  - Containment: Immediately block the principal: revoke sessions (invalidate tokens at the identity provider), rotate access and secret keys, and remove or restrict the principal's IAM permissions (attach a deny policy or remove role trust). As an intermediate step, restrict the affected principal's network access by updating security groups or NACLs to block outbound data paths. Log every containment step in CloudTrail and the incident ticket.
  - Recovery and validation: Conduct an access review to identify what actions the principal performed (CloudTrail) and what data was accessed (application logs, RDS logs). Rebuild affected artifacts if the build pipeline could be compromised. Execute a post-incident review and update detection thresholds and playbooks.

- Malicious container image deployed
  - Detection: Detection may originate from multiple signals: Inspector/ECR reports a new critical vulnerability for an image in production; runtime behavioral agents (Falco) detect suspicious process execution (for example, a spawn of `/bin/sh` in a read-only container or unexpected outbound connections); GuardDuty flags anomalous DNS or data egress patterns.
  - Containment: Stop or cordon the affected task(s) and deploy a replacement using the previously approved image digest. Place the suspected ECR repository into read-only mode by adjusting repository policies or temporarily disabling pushes. If appropriate, isolate the subnet or update security groups to block egress from affected tasks.
  - Forensics: Preserve the ECR image manifest and SBOM, copy container logs and runtime traces to an immutable forensics bucket, capture task metadata (task ARN, container ID, environment variables masked), and, if data access is suspected, create an encrypted RDS snapshot for off-line analysis. Record timestamps and related CloudTrail/VPC Flow Logs for correlation.


3.4 Testing and validation

- Table-top exercises & red-team
  - Schedule regular exercises simulating credential compromise and malicious image deployment; validate automated containment playbooks work and measure Mean Time To Contain (MTTC).

---

## 4. Application-specific notes (Firebase + Spring Boot)

- Firebase authentication
  - Firebase handles authentication; ensure your application logs user IDs and authentication metadata (token issuance time, token expiry, provider, and claims) and correlates them with backend authorization decisions.
  - For audit trails, export Firebase authentication logs or ensure application-level logging captures key events.

- Spring Boot backend
  - Log authentication/authorization decisions, input validation failures, suspicious payloads (rate-limited to avoid log pollution), and data-access events involving PHI. Use structured logs with request IDs to correlate across services.

---

## 5. Evidence and artifact checklist (what to provide to auditors)

- CloudTrail logs and S3 trail configuration
- GuardDuty and Inspector findings history
- Security Hub aggregated findings and evidence attachments
- CloudWatch Logs streams for application and container logs
- VPC Flow Logs and RDS logs for the audit window
- ECR image scan reports and SBOMs, image digests deployed in production
- IAM policy and role exports, MFA enforcement evidence
- Documentation of EventBridge containment rules, Lambda playbooks, and runbooks executed during incidents

---

## 6. Implementation pointers and quick commands

Below are sample console/CLI pointers (non-exhaustive):

- Enable an organization trail (CloudTrail):

```bash
aws cloudtrail create-trail --name org-trail --s3-bucket-name my-secure-audit-bucket --is-organization-trail
aws cloudtrail start-logging --name org-trail
```

- Enable Amazon ECR scan on push for a repository (example):

```bash
aws ecr put-image-scanning-configuration --repository-name my-repo --image-scanning-configuration scanOnPush=true
```

- Create a GuardDuty detector (centralized):

```bash
aws guardduty create-detector --enable
```

---

Prepared by: Infrastructure / Security automation
Date: 2025-10-07
