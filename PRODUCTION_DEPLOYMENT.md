# SAML Framework - Production Deployment Guide

## 🚀 Overview

This guide provides comprehensive instructions for deploying the SAML framework in a production environment with full security measures.

## 📋 Prerequisites

- Java 8 or higher
- Maven 3.6+
- OpenSSL (for certificate generation)
- Access to your Identity Provider (IdP)
- Production server/container

## 🔐 Security Features

### Production Security Implemented:

1. **SAML-Specific Security**
   - Replay attack protection
   - Request ID uniqueness validation
   - Response time validation
   - Signature validation (configurable)
   - Session management with timeout

2. **HTTP Security Headers**
   - X-Frame-Options: DENY
   - X-Content-Type-Options: nosniff
   - X-XSS-Protection: 1; mode=block
   - Content Security Policy (CSP)
   - Referrer Policy
   - Permissions Policy

3. **Session Security**
   - 30-minute session timeout
   - Secure cookie settings
   - Session invalidation on logout

4. **Certificate Management**
   - X.509 certificate support
   - BouncyCastle security provider
   - Certificate validation

## 🛠️ Production Setup

### Step 1: Generate Certificates

```bash
# Run the certificate generation script
./scripts/generate-certificates.sh
```

This will create:
- `certs/sp-certificate.pem` - Service Provider certificate
- `certs/sp-private-key.pem` - Service Provider private key
- `keystore/sp-keystore.p12` - PKCS12 keystore
- `keystore/sp-keystore.jks` - JKS keystore

### Step 2: Configure Identity Provider

1. **Configure your IdP with the SP metadata:**
   ```
   Entity ID: http://your-domain.com/saml/metadata
   ACS URL: http://your-domain.com/saml/acs
   SLO URL: http://your-domain.com/saml/slo
   ```

2. **Upload the SP certificate** to your IdP

### Step 3: Update Configuration

Edit `src/main/resources/application.properties`:

```properties
# Production SAML Configuration
saml.entity-id=http://your-domain.com/saml/metadata
saml.assertion-consumer-service-url=http://your-domain.com/saml/acs
saml.single-logout-service-url=http://your-domain.com/saml/slo

# Your IdP Configuration
saml.idp.entity-id=http://your-idp-entity-id
saml.idp.single-sign-on-service-url=http://your-idp-sso-url
saml.idp.single-logout-service-url=http://your-idp-slo-url
saml.idp.x509-certificate=-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----

# Your SP Certificate and Key
saml.sp.x509-certificate=-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----
saml.sp.private-key=-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----

# Production Security Settings
saml.security.replay-attack-protection=true
saml.security.max-response-age-minutes=5
saml.security.session-timeout-minutes=30
saml.security.require-signatures=true
```

### Step 4: Build for Production

```bash
# Clean and build
./mvnw clean package -DskipTests

# Create production JAR
./mvnw spring-boot:repackage
```

### Step 5: Deploy

#### Option A: Standalone JAR
```bash
java -jar target/opensaml-0.0.1-SNAPSHOT.jar
```

#### Option B: Docker
```dockerfile
FROM openjdk:8-jre-alpine
COPY target/opensaml-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

#### Option C: Application Server
Deploy the WAR file to Tomcat, JBoss, or WebSphere.

## 🔍 Monitoring and Health Checks

### Health Endpoints
- `GET /actuator/health` - Application health
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/prometheus` - Prometheus metrics

### Logging
- Application logs: `logs/opensaml.log`
- Log rotation: 100MB max, 30 days retention
- Log level: INFO for production

### Metrics
- SAML request/response counts
- Session statistics
- Error rates
- Response times

## 🛡️ Security Checklist

### Before Deployment:
- [ ] Generate production certificates (not self-signed)
- [ ] Configure HTTPS/TLS
- [ ] Set up firewall rules
- [ ] Configure IdP with correct SP metadata
- [ ] Test SAML flows in staging environment
- [ ] Review and update security headers
- [ ] Set up monitoring and alerting

### Runtime Security:
- [ ] Monitor for failed authentication attempts
- [ ] Check for replay attacks
- [ ] Validate session timeouts
- [ ] Monitor certificate expiration
- [ ] Review access logs regularly

## 🔧 Configuration Options

### SAML Security Settings
```properties
# Replay attack protection
saml.security.replay-attack-protection=true

# Maximum response age (minutes)
saml.security.max-response-age-minutes=5

# Session timeout (minutes)
saml.security.session-timeout-minutes=30

# Require signed assertions
saml.security.require-signatures=true

# Require encrypted assertions
saml.security.require-encryption=false
```

### Server Configuration
```properties
# Thread pool settings
server.tomcat.max-threads=200
server.tomcat.min-spare-threads=10
server.tomcat.connection-timeout=20000

# Session settings
server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict
```

## 🚨 Troubleshooting

### Common Issues:

1. **Certificate Issues**
   - Verify certificate format (PEM/DER)
   - Check certificate expiration
   - Ensure proper key usage extensions

2. **SAML Response Validation**
   - Check response time validity
   - Verify issuer matches configuration
   - Validate signature (if required)

3. **Session Issues**
   - Check session timeout settings
   - Verify cookie settings
   - Monitor session cleanup

4. **Network Issues**
   - Verify IdP accessibility
   - Check firewall rules
   - Validate DNS resolution

### Debug Mode
For troubleshooting, enable debug logging:
```properties
logging.level.com.saml.server.opensaml=DEBUG
logging.level.org.opensaml=DEBUG
```

## 📞 Support

For production issues:
1. Check application logs
2. Review SAML response/request logs
3. Verify IdP configuration
4. Test with SAML tracer browser extension

## 🔄 Maintenance

### Regular Tasks:
- Monitor certificate expiration
- Review security logs
- Update dependencies
- Backup configuration
- Test disaster recovery

### Updates:
- Keep OpenSAML library updated
- Monitor security advisories
- Update certificates before expiration
- Review and update security configurations

---

**⚠️ Important:** This is a production-ready SAML framework. Always test thoroughly in a staging environment before deploying to production.
