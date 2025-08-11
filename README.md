# OpenSAML 3 Custom SAML Framework

A comprehensive SAML framework built with OpenSAML 3, Spring Boot 1.4.2, and Java 8. This framework provides a complete SAML Service Provider (SP) implementation with support for authentication, logout, and metadata generation.

## Features

- **SAML 2.0 Support**: Full implementation of SAML 2.0 protocol
- **Service Provider (SP)**: Acts as a SAML Service Provider
- **Authentication**: SP-initiated Single Sign-On (SSO)
- **Logout**: Single Logout (SLO) support
- **Multiple Bindings**: HTTP-POST and HTTP-Redirect binding support
- **Signature Support**: XML signature creation and validation
- **Web Interface**: Built-in test interface for SAML operations
- **REST API**: RESTful endpoints for SAML operations
- **Configuration**: Externalized configuration via properties file

## Prerequisites

- Java 8
- Maven 3.6+
- Spring Boot 1.4.2

## Dependencies

The framework uses the following key dependencies:

- **OpenSAML 3.4.6**: Core SAML library
- **Spring Boot 1.4.2**: Application framework
- **Spring Security**: Security framework
- **Thymeleaf**: Template engine for web interface
- **Apache Commons**: Utility libraries

## Project Structure

```
src/main/java/com/saml/server/opensaml/
├── config/
│   ├── OpenSAMLConfig.java          # OpenSAML initialization
│   └── SAMLProperties.java          # Configuration properties
├── service/
│   ├── SAMLUtilityService.java      # Utility functions
│   ├── SAMLAuthRequestService.java  # Authentication requests
│   ├── SAMLResponseService.java     # Response processing
│   └── SAMLLogoutService.java       # Logout handling
├── controller/
│   ├── SAMLController.java          # REST API endpoints
│   └── WebController.java           # Web interface
└── OpensamlApplication.java         # Main application class
```

## Configuration

### 1. Update application.properties

Configure your SAML settings in `src/main/resources/application.properties`:

```properties
# Service Provider Configuration
saml.entity-id=http://localhost:8080/saml/metadata
saml.assertion-consumer-service-url=http://localhost:8080/saml/acs
saml.single-logout-service-url=http://localhost:8080/saml/slo

# Identity Provider Configuration
saml.idp.entity-id=http://your-idp-entity-id
saml.idp.single-sign-on-service-url=http://your-idp-sso-url
saml.idp.single-logout-service-url=http://your-idp-slo-url
saml.idp.x509-certificate=your-idp-certificate-here

# Service Provider Certificate and Key
saml.sp.x509-certificate=your-sp-certificate-here
saml.sp.private-key=your-sp-private-key-here
```

### 2. Generate Certificates

You'll need to generate X.509 certificates for your Service Provider:

```bash
# Generate private key
openssl genrsa -out sp-private-key.pem 2048

# Generate certificate
openssl req -new -x509 -key sp-private-key.pem -out sp-certificate.pem -days 365
```

## Running the Application

### 1. Build the project

```bash
./mvnw clean install
```

### 2. Run the application

```bash
./mvnw spring-boot:run
```

### 3. Access the web interface

Open your browser and navigate to: `http://localhost:8080`

## API Endpoints

### Authentication

- **GET** `/saml/login` - Initiate SAML SSO
- **POST** `/saml/acs` - Handle SAML Response (POST binding)
- **GET** `/saml/acs` - Handle SAML Response (Redirect binding)

### Logout

- **GET** `/saml/logout` - Initiate SAML Logout
- **POST** `/saml/slo` - Handle Logout Request
- **POST** `/saml/slo-response` - Handle Logout Response

### Metadata

- **GET** `/saml/metadata` - Get SAML metadata

## Usage Examples

### 1. Initiate SAML SSO

```bash
curl "http://localhost:8080/saml/login?relayState=test123"
```

### 2. Process SAML Response

```bash
curl -X POST "http://localhost:8080/saml/acs" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "SAMLResponse=base64-encoded-response&RelayState=test123"
```

### 3. Initiate Logout

```bash
curl "http://localhost:8080/saml/logout?nameId=user@example.com&sessionIndex=session123"
```

### 4. Get Metadata

```bash
curl "http://localhost:8080/saml/metadata"
```

## Web Interface

The application includes a web-based test interface that allows you to:

1. **Initiate SAML SSO**: Start the authentication process
2. **Process SAML Responses**: Test response processing with sample data
3. **Test Logout**: Initiate and test logout functionality
4. **View Metadata**: Display SAML metadata information

## Integration with Identity Providers

### Common IdP Configurations

#### 1. Okta

```properties
saml.idp.entity-id=http://www.okta.com/exk123456
saml.idp.single-sign-on-service-url=https://your-domain.okta.com/app/your-app/exk123456/sso/saml
saml.idp.single-logout-service-url=https://your-domain.okta.com/app/your-app/exk123456/slo/saml
```

#### 2. Azure AD

```properties
saml.idp.entity-id=https://sts.windows.net/your-tenant-id/
saml.idp.single-sign-on-service-url=https://login.microsoftonline.com/your-tenant-id/saml2
saml.idp.single-logout-service-url=https://login.microsoftonline.com/your-tenant-id/saml2
```

#### 3. ADFS

```properties
saml.idp.entity-id=http://your-adfs-server/adfs/services/trust
saml.idp.single-sign-on-service-url=https://your-adfs-server/adfs/ls
saml.idp.single-logout-service-url=https://your-adfs-server/adfs/ls
```

## Security Considerations

1. **HTTPS**: Always use HTTPS in production
2. **Certificate Management**: Properly manage and rotate certificates
3. **Signature Validation**: Always validate SAML signatures
4. **Session Management**: Implement proper session handling
5. **Error Handling**: Implement comprehensive error handling
6. **Logging**: Monitor and log SAML operations

## Troubleshooting

### Common Issues

1. **OpenSAML Initialization Error**: Ensure OpenSAML is properly initialized
2. **Certificate Issues**: Verify certificate format and validity
3. **URL Mismatches**: Check that all URLs match between SP and IdP
4. **Signature Validation**: Ensure certificates are properly configured

### Debug Mode

Enable debug logging by adding to `application.properties`:

```properties
logging.level.com.saml.server.opensaml=DEBUG
logging.level.org.opensaml=DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:

1. Check the troubleshooting section
2. Review the OpenSAML documentation
3. Create an issue in the repository

## References

- [SAML 2.0 Specification](http://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html)
- [OpenSAML Documentation](https://shibboleth.atlassian.net/wiki/spaces/OSAML/overview)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
