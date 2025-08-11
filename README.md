# SAML Framework with OpenSAML 3

A production-ready SAML 2.0 Service Provider framework built with Spring Boot 1.4.2 and OpenSAML 3.4.6, designed for enterprise-grade Single Sign-On (SSO) integration.

## 🚀 Features

### Core SAML Functionality
- ✅ **SAML 2.0 Protocol Support** - Full implementation of SAML 2.0 specification
- ✅ **SP-Initiated SSO** - Service Provider initiated Single Sign-On
- ✅ **IdP-Initiated SSO** - Identity Provider initiated Single Sign-On
- ✅ **Single Logout (SLO)** - Complete logout across all applications
- ✅ **HTTP-POST & HTTP-Redirect Bindings** - Support for both SAML bindings
- ✅ **SAML Metadata** - Dynamic metadata generation

### Production Security
- 🔐 **Replay Attack Protection** - Prevents SAML response replay attacks
- 🔐 **Request ID Validation** - Ensures unique request IDs
- 🔐 **Response Time Validation** - Validates SAML response timestamps
- 🔐 **Session Management** - Secure session handling with timeout
- 🔐 **Certificate Management** - X.509 certificate support
- 🔐 **Security Headers** - Comprehensive HTTP security headers

### Monitoring & Operations
- 📊 **Health Checks** - Application health monitoring
- 📊 **Metrics** - SAML operation metrics
- 📊 **Logging** - Structured logging with rotation
- 📊 **Actuator Endpoints** - Spring Boot Actuator integration

## 🛠️ Technology Stack

- **Java 8** - Runtime environment
- **Spring Boot 1.4.2** - Application framework
- **OpenSAML 3.4.6** - SAML implementation
- **Maven** - Build tool
- **Thymeleaf** - Template engine
- **BouncyCastle** - Cryptographic operations

## 📋 Prerequisites

- Java 8 or higher
- Maven 3.6+
- OpenSSL (for certificate generation)
- Access to an Identity Provider (IdP)

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/Somesh-Kumar-Yadav/saml-withopensaml.git
cd saml-withopensaml
```

### 2. Generate Certificates
```bash
chmod +x scripts/generate-certificates.sh
./scripts/generate-certificates.sh
```

### 3. Configure Your IdP
Update `src/main/resources/application.properties` with your Identity Provider details:
```properties
# Your IdP Configuration
saml.idp.entity-id=http://your-idp-entity-id
saml.idp.single-sign-on-service-url=http://your-idp-sso-url
saml.idp.single-logout-service-url=http://your-idp-slo-url
saml.idp.x509-certificate=-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----
```

### 4. Build and Run
```bash
./mvnw clean package
java -jar target/opensaml-0.0.1-SNAPSHOT.jar
```

### 5. Access the Application
- **Application**: http://localhost:8080
- **SAML Metadata**: http://localhost:8080/saml/metadata
- **Health Check**: http://localhost:8080/actuator/health

## 📡 API Endpoints

### SAML Endpoints
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/saml/metadata` | GET | SAML metadata for IdP configuration |
| `/saml/login` | GET | Initiate SAML SSO |
| `/saml/acs` | POST/GET | Handle SAML response |
| `/saml/logout` | GET | Initiate SAML logout |
| `/saml/slo` | POST | Handle logout request |
| `/saml/slo-response` | POST | Handle logout response |

### Monitoring Endpoints
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/actuator/health` | GET | Application health status |
| `/actuator/metrics` | GET | Application metrics |
| `/actuator/prometheus` | GET | Prometheus metrics |

## 🔐 Security Features

### SAML Security
- **Replay Attack Protection**: Prevents duplicate SAML responses
- **Request ID Validation**: Ensures unique request identifiers
- **Response Time Validation**: Validates SAML response timestamps (5-minute window)
- **Session Management**: 30-minute session timeout with cleanup
- **Certificate Validation**: X.509 certificate support

### HTTP Security Headers
```http
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Content-Security-Policy: default-src 'self'
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: geolocation=(), microphone=(), camera=()
```

## 📁 Project Structure

```
saml-withopensaml/
├── src/main/java/com/saml/server/opensaml/
│   ├── config/
│   │   ├── OpenSAMLConfig.java          # OpenSAML initialization
│   │   ├── ProductionSecurityFilter.java # Security headers
│   │   └── SAMLProperties.java          # Configuration properties
│   ├── controller/
│   │   ├── SAMLController.java          # SAML REST endpoints
│   │   └── WebController.java           # Web interface
│   ├── service/
│   │   ├── SAMLAuthRequestService.java  # AuthnRequest creation
│   │   ├── SAMLResponseService.java     # Response processing
│   │   ├── SAMLLogoutService.java       # Logout handling
│   │   ├── SAMLSecurityService.java     # Security validation
│   │   └── SAMLUtilityService.java      # Utility functions
│   └── OpensamlApplication.java         # Main application
├── src/main/resources/
│   ├── application.properties           # Configuration
│   └── templates/index.html             # Web interface
├── scripts/
│   └── generate-certificates.sh         # Certificate generation
├── PRODUCTION_DEPLOYMENT.md             # Deployment guide
└── README.md                            # This file
```

## 🔧 Configuration

### SAML Configuration
```properties
# Service Provider Configuration
saml.entity-id=http://localhost:8080/saml/metadata
saml.assertion-consumer-service-url=http://localhost:8080/saml/acs
saml.single-logout-service-url=http://localhost:8080/saml/slo

# Identity Provider Configuration
saml.idp.entity-id=http://your-idp-entity-id
saml.idp.single-sign-on-service-url=http://your-idp-sso-url
saml.idp.single-logout-service-url=http://your-idp-slo-url

# Security Settings
saml.security.replay-attack-protection=true
saml.security.max-response-age-minutes=5
saml.security.session-timeout-minutes=30
saml.security.require-signatures=true
```

### Production Settings
```properties
# Server Configuration
server.port=8080
server.tomcat.max-threads=200
server.servlet.session.timeout=30m

# Logging
logging.level.com.saml.server.opensaml=INFO
logging.file.name=logs/opensaml.log
logging.file.max-size=100MB
```

## 🚀 Production Deployment

For production deployment, see the comprehensive guide in [PRODUCTION_DEPLOYMENT.md](PRODUCTION_DEPLOYMENT.md).

### Key Production Steps:
1. Generate production certificates (not self-signed)
2. Configure HTTPS/TLS
3. Set up firewall rules
4. Configure monitoring and alerting
5. Test thoroughly in staging environment

## 🧪 Testing

### Manual Testing
1. Access http://localhost:8080
2. Use the web interface to test SAML flows
3. Check logs for detailed information

### API Testing
```bash
# Get SAML metadata
curl http://localhost:8080/saml/metadata

# Initiate SSO
curl "http://localhost:8080/saml/login?relayState=test123"

# Check health
curl http://localhost:8080/actuator/health
```

## 🔍 Troubleshooting

### Common Issues

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

### Debug Mode
Enable debug logging for troubleshooting:
```properties
logging.level.com.saml.server.opensaml=DEBUG
logging.level.org.opensaml=DEBUG
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For issues and questions:
1. Check the [PRODUCTION_DEPLOYMENT.md](PRODUCTION_DEPLOYMENT.md) guide
2. Review application logs
3. Test with SAML tracer browser extension
4. Open an issue on GitHub

## 🔗 Related Links

- [SAML 2.0 Specification](http://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html)
- [OpenSAML Documentation](https://shibboleth.atlassian.net/wiki/spaces/OSAML/overview)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)

---

**⭐ Star this repository if you find it helpful!**

**⚠️ Important**: This is a production-ready SAML framework. Always test thoroughly in a staging environment before deploying to production.
