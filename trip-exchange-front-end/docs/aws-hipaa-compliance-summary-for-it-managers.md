# AWS and HIPAA: Executive summary for IT Managers

Audience: IT Manager / Infrastructure Lead

Purpose: Describe how AWS helps meet HIPAA requirements for an application deployed in a dedicated VPC and using an encrypted Amazon RDS (MySQL) instance. Provide the controls, evidence sources, and recommended next steps for audit readiness.

---

## 1) High-level context

- The application is deployed in AWS inside a logically isolated Amazon VPC. Network segmentation, security groups, and NACLs are used to control inbound/outbound traffic.
- Persistent patient data is stored in an encrypted Amazon RDS (MySQL) instance. Encryption at rest is provided by AWS KMS-managed CMKs (customer-managed or AWS-managed keys) as configured for RDS.
- Audit and activity logs are captured using AWS CloudTrail (management and data events), and system-level and OS logs can be collected with CloudWatch Logs. AWS Config, Security Hub, GuardDuty and Audit Manager are available to support continuous compliance monitoring and evidence collection.

This environment is consistent with AWS guidance for deploying HIPAA-eligible workloads. See Amazon RDS compliance and the AWS HIPAA architecting guidance for detailed mappings.

References (authoritative):

- Amazon RDS compliance documentation: [Amazon RDS compliance](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/RDS-compliance.html)
- Architecting for HIPAA Security and Compliance on AWS (AWS whitepaper): [Architecting for HIPAA Security and Compliance on AWS](https://docs.aws.amazon.com/whitepapers/latest/architecting-hipaa-security-and-compliance-on-aws/architecting-hipaa-security-and-compliance-on-aws.html)
- AWS Config: Operational best practices for HIPAA security: [Operational best practices for HIPAA security](https://docs.aws.amazon.com/config/latest/developerguide/operational-best-practices-for-hipaa_security.html)
- AWS CloudTrail compliance notes: [CloudTrail compliance](https://docs.aws.amazon.com/cloudtrail/latest/userguide/CloudTrail-compliance.html)
- AWS KMS compliance: [AWS KMS compliance](https://docs.aws.amazon.com/kms/latest/developerguide/kms-compliance.html)

---

## 2) Shared Responsibility Model (brief)

1. AWS responsibility (what AWS provides and attests):
   - Physical security of datacenters, hypervisor/Nitro isolation, and many foundational controls. AWS undergoes independent third-party audits covering HIPAA for in-scope services and provides those reports via AWS Artifact.
   - Native service controls and features (for example, RDS encryption at rest using KMS, VPC network isolation, CloudTrail, etc.) are implemented and operated by AWS.

2. Customer responsibility (what you must do):
   - Configure services securely (enable encryption, network controls, IAM policies, logging).
   - Manage data access, user accounts, application-level encryption/tokenization where needed, key lifecycle if using customer-managed keys, and incident response.
   - Execute and maintain a Business Associate Agreement (BAA) with AWS before storing PHI in AWS services.

Note: HIPAA compliance is not a single checkbox — it’s a combination of controls, processes, and documentation that you must maintain. AWS provides compliant building blocks and evidence to demonstrate the controls they operate.

---

## 3) How your current architecture maps to HIPAA control areas

Below are common HIPAA Security Rule domains with the AWS features in use (or recommended) and the kinds of evidence to collect for an audit.

- Access Control (HIPAA: §164.312(a))
  - AWS features: IAM with least-privilege policies, MFA for privileged user access, IAM role separation, RDS IAM authentication (optional), security groups limiting access to DB ports.
  - Evidence: IAM policies and roles exports, MFA enforcement logs, Security Group rules, IAM Access Analyzer reports, CloudTrail records showing privileged access.

- Audit Controls (HIPAA: §164.312(b))
  - AWS features: CloudTrail (management & data events), RDS enhanced auditing (MySQL general / slow / audit logs), CloudWatch Logs retention and archival to S3.
  - Evidence: CloudTrail log buckets (with access policies), sample trails showing relevant events, RDS audit logs, change history from AWS Config.

- Integrity Controls (HIPAA: §164.312(c))
  - AWS features: Database backups & automated snapshots, RDS automated minor version patching (if enabled), encryption (KMS) to protect tamper evidence, checksums at DB level as appropriate.
  - Evidence: Backup retention configuration, snapshot encryption settings, Config rule evaluations, DB parameter group settings.

- Transmission Security (HIPAA: §164.312(e))
  - AWS features: Enforce TLS for application-to-database connections (SSL for MySQL), VPC endpoints to avoid public internet for service traffic, use of ELBs/ALBs with TLS termination.
  - Evidence: DB parameter settings enforcing SSL, load balancer certificate configuration, VPC endpoint configurations, network diagrams showing private connectivity.

- Encryption (addressing both at-rest and in-transit requirements)
  - At-rest: RDS encryption using KMS-managed keys (customer-managed recommended for key control). Ensure automated snapshots and read replicas are also encrypted.
  - In-transit: Enforce TLS for all connections; use secure application libraries; use VPC endpoints/private subnets to reduce exposure.
  - Evidence: KMS key policy, key rotation configuration, RDS encryption flag, snapshot encryption metadata, TLS configuration in app and DB, Certificate Manager records if used.

- Security Incident Procedures (HIPAA: §164.308(a)(6))
  - AWS features: GuardDuty, Security Hub, CloudWatch Alarms, EventBridge rules to escalate, AWS Config change notifications, S3 access logging.
  - Evidence: Incident response playbooks, sample GuardDuty findings & remediation tickets, CloudTrail events correlated to an incident, Audit Manager assessment reports.

---

## 4) Evidence sources & artifacts to gather for an audit

- AWS Artifact: access to AWS third-party audit reports and the BAA. (Sign BAA via Artifact if not already done.)
- AWS Config: snapshot of configuration, Config rule evaluations (use the HIPAA mapping rules workbook).
- AWS CloudTrail: centralized trails for management and data events; logs retained and access-controlled in S3.
- AWS KMS: key metadata, key policy, key rotation schedule, usage logs (CloudTrail events for kms:GenerateDataKey, Encrypt, Decrypt).
- Amazon RDS: instance configuration (storage encryption flag), snapshots (encrypted), parameter group, enhanced auditing logs if enabled.
- IAM: policy and role exports, MFA enforcement status, access keys usage (CloudTrail), IAM Access Analyzer outputs.
- Network artifacts: VPC flow logs, security group and NACL configuration, subnet diagrams, route tables and NAT/IGW configs.
- Monitoring & detection: GuardDuty findings, Security Hub aggregated findings, CloudWatch alarms history, Audit Manager assessments.

---

## 5) Quick checklist: minimum actions to ensure HIPAA-readiness for your current setup

1. Confirm AWS BAA is executed and current (AWS Artifact).
2. Ensure RDS encryption at rest is enabled and backed by a customer-managed KMS key if you require stricter key control and separation of duties.
3. Enable and centralize CloudTrail (management + data events) to a locked-down S3 audit bucket with lifecycle/retention aligned to policy and legal requirements.
4. Turn on RDS logging (general/slow/audit) and ship logs to CloudWatch Logs / S3 for retention and analysis.
5. Enforce TLS for all in-transit connections and validate DB parameter group settings for ssl_mode.
6. Use IAM least privilege, enable MFA for all console users, and rotate/disable long-lived credentials.
7. Deploy AWS Config rules mapped to HIPAA controls and remediate rule violations; keep periodic evidence snapshots.
8. Enable GuardDuty and Security Hub for detection and continuous monitoring; archive findings and remediation actions.
9. Document incident response procedures and run tabletop exercises; record results.
10. Prepare a document packet for auditors with links to Artifact reports, Config snapshots, CloudTrail trails, KMS key metadata, and RDS encryption/snapshot evidence.

---

## 6) Recommendations & next steps (operational)

- Consider a multi-account AWS Organization approach: separate log archive, security tooling, network, and application workloads to simplify segregation of duties and evidence collection.
- Use AWS Audit Manager with a HIPAA framework to automate evidence collection and generate audit reports.
- If you need more confidentiality assurance, adopt customer-managed KMS CMKs with strict key policies and separate key administrators from data administrators.
- Implement automated compliance-as-code: use AWS Config, Security Hub custom actions, and Infrastructure as Code templates (CloudFormation/Terraform) to standardize and version control secure baselines.
- Consider database-level additional protections such as field-level encryption, tokenization, or pseudonymization for PHI elements if required by policy.

---

## 7) For the auditor: where to find authoritative AWS documents

- AWS Artifact (BAA and third-party audit reports): [AWS Artifact](https://aws.amazon.com/artifact/)
- HIPAA whitepaper (architecting guidance): [Architecting for HIPAA Security and Compliance on AWS](https://docs.aws.amazon.com/whitepapers/latest/architecting-hipaa-security-and-compliance-on-aws/architecting-hipaa-security-and-compliance-on-aws.html)
- Services compliance pages: Amazon RDS, VPC, KMS, CloudTrail (see references in section 1)
- AWS Config operational best practices for HIPAA: [Operational best practices for HIPAA security](https://docs.aws.amazon.com/config/latest/developerguide/operational-best-practices-for-hipaa_security.html)

---

## 8) Summary / bottom line

AWS provides the in-scope services, infrastructure controls, and independent third-party audit evidence to support HIPAA workloads. Maintaining HIPAA compliance for your application requires proper configuration of those services, ongoing operational processes (monitoring, patching, access control), and documentation. With RDS encryption, VPC isolation, centralized logging (CloudTrail), KMS key management, and use of Config/GuardDuty/Security Hub, your current architecture aligns with recommended HIPAA controls — provided the customer-side responsibilities (BAA, secure configuration, access management, incident response) are fully implemented and evidenced.

---

Prepared by: Infrastructure / Security automation
Date: 2025-10-07
