# SAML Service Provider Metadata Guide

## 📋 Overview

This guide explains the SAML Service Provider (SP) metadata files and how to configure them for different environments. The metadata files contain all the necessary information for Identity Providers (IdPs) to configure your SAML Service Provider.

## 📁 Metadata Files

### 1. Development Metadata (`saml-metadata.xml`)
- **Location**: `src/main/resources/static/saml-metadata.xml`
- **Purpose**: Development and testing environment
- **URLs**: HTTP (localhost:8080)
- **Security**: Basic security settings

### 2. Production Metadata (`saml-metadata-production.xml`)
- **Location**: `src/main/resources/static/saml-metadata-production.xml`
- **Purpose**: Production environment
- **URLs**: HTTPS (your-domain.com)
- **Security**: Enhanced security settings

## 🔧 Configuration Steps

### Step 1: Customize the Metadata

#### For Development:
1. Update the `entityID` in `saml-metadata.xml`:
   ```xml
   entityID="http://localhost:8080/saml/metadata"
   ```

2. Update organization information:
   ```xml
   <md:OrganizationName xml:lang="en">Your Organization Name</md:OrganizationName>
   <md:OrganizationDisplayName xml:lang="en">Your Application SP</md:OrganizationDisplayName>
   <md:OrganizationURL xml:lang="en">http://localhost:8080</md:OrganizationURL>
   ```

3. Update contact information:
   ```xml
   <md:EmailAddress>your-email@example.com</md:EmailAddress>
   ```

#### For Production:
1. Update the `entityID` in `saml-metadata-production.xml`:
   ```xml
   entityID="https://your-domain.com/saml/metadata"
   ```

2. Update all URLs to use your domain:
   ```xml
   Location="https://your-domain.com/saml/acs"
   Location="https://your-domain.com/saml/slo"
   ```

3. Add your SP certificate:
   ```xml
   <md:KeyDescriptor use="signing">
       <ds:KeyInfo>
           <ds:X509Data>
               <ds:X509Certificate>
                   YOUR_ACTUAL_SP_CERTIFICATE_HERE
               </ds:X509Certificate>
           </ds:X509Data>
       </ds:KeyInfo>
   </md:KeyDescriptor>
   ```

### Step 2: Generate SP Certificate (Production)

```bash
# Generate private key
openssl genrsa -out sp-private-key.pem 2048

# Generate certificate signing request
openssl req -new -key sp-private-key.pem -out sp-certificate.csr

# Generate self-signed certificate (for testing)
openssl x509 -req -in sp-certificate.csr -signkey sp-private-key.pem -out sp-certificate.pem -days 365

# Or get certificate from CA (for production)
# Submit CSR to your Certificate Authority
```

### Step 3: Extract Certificate for Metadata

```bash
# Extract certificate content (remove headers/footers)
openssl x509 -in sp-certificate.pem -outform PEM -out sp-certificate-clean.pem

# Copy the certificate content (between BEGIN and END markers)
cat sp-certificate-clean.pem
```

### Step 4: Update Metadata with Certificate

Replace `YOUR_SP_SIGNING_CERTIFICATE_HERE` with your actual certificate content:

```xml
<ds:X509Certificate>
    MIIEpDCCA4ygAwIBAgIJANx8t6tqBzqDMA0GCSqGSIb3DQEBCwUAMIGLMQswCQYD
    VQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4g
    VmlldzEQMA4GA1UEChMHVW5rbm93bjEQMA4GA1UECxMHVW5rbm93bjEQMA4GA1UE
    AxMHVW5rbm93bjEgMB4GCSqGSIb3DQEJARYRdW5rbm93bkBleGFtcGxlLmNvbTAe
    Fw0yNTAxMTUxMDMwMDBaFw0yNjAxMTUxMDMwMDBaMIGLMQswCQYDVQQGEwJVUzET
    MBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEQMA4G
    A1UEChMHVW5rbm93bjEQMA4GA1UECxMHVW5rbm93bjEQMA4GA1UEAxMHVW5rbm93
    bjEgMB4GCSqGSIb3DQEJARYRdW5rbm93bkBleGFtcGxlLmNvbTCCASIwDQYJKoZI
    hvcNAQEBBQADggEPADCCAQoCggEBAL...
</ds:X509Certificate>
```

## 📡 Metadata Endpoints

### Dynamic Metadata (Recommended)
Your application provides dynamic metadata at:
- **Development**: `http://localhost:8080/saml/metadata`
- **Production**: `https://your-domain.com/saml/metadata`

### Static Metadata Files
You can also serve static metadata files:
- **Development**: `http://localhost:8080/saml-metadata.xml`
- **Production**: `https://your-domain.com/saml-metadata.xml`

## 🔐 Security Settings

### Development Settings
```xml
AuthnRequestsSigned="false"
WantAssertionsSigned="true"
```

### Production Settings
```xml
AuthnRequestsSigned="true"
WantAssertionsSigned="true"
WantAssertionsEncrypted="false"
```

## 📋 Metadata Components Explained

### 1. Entity Descriptor
```xml
<md:EntityDescriptor entityID="https://your-domain.com/saml/metadata"
                     validUntil="2026-12-31T23:59:59Z">
```
- **entityID**: Unique identifier for your SP
- **validUntil**: Metadata expiration date

### 2. Organization Information
```xml
<md:Organization>
    <md:OrganizationName xml:lang="en">Your Organization Name</md:OrganizationName>
    <md:OrganizationDisplayName xml:lang="en">Your Application SP</md:OrganizationDisplayName>
    <md:OrganizationURL xml:lang="en">https://your-domain.com</md:OrganizationURL>
</md:Organization>
```

### 3. Contact Information
```xml
<md:ContactPerson contactType="technical">
    <md:GivenName>Technical</md:GivenName>
    <md:SurName>Support</md:SurName>
    <md:EmailAddress>tech-support@your-domain.com</md:EmailAddress>
</md:ContactPerson>
```

### 4. Service Endpoints

#### Assertion Consumer Service (ACS)
```xml
<md:AssertionConsumerService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
                             Location="https://your-domain.com/saml/acs"
                             index="0"
                             isDefault="true"/>
```

#### Single Logout Service (SLO)
```xml
<md:SingleLogoutService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
                        Location="https://your-domain.com/saml/slo"
                        ResponseLocation="https://your-domain.com/saml/slo-response"/>
```

### 5. Name ID Formats
```xml
<md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress</md:NameIDFormat>
<md:NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:persistent</md:NameIDFormat>
<md:NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:transient</md:NameIDFormat>
```

### 6. Requested Attributes
```xml
<md:AttributeConsumingService index="0" isDefault="true">
    <md:ServiceName xml:lang="en">Your Application</md:ServiceName>
    <md:ServiceDescription xml:lang="en">Main application service</md:ServiceDescription>
    
    <!-- Required Attributes -->
    <md:RequestedAttribute Name="email" isRequired="true"/>
    <md:RequestedAttribute Name="firstName" isRequired="true"/>
    <md:RequestedAttribute Name="lastName" isRequired="true"/>
    
    <!-- Optional Attributes -->
    <md:RequestedAttribute Name="department" isRequired="false"/>
    <md:RequestedAttribute Name="groups" isRequired="false"/>
</md:AttributeConsumingService>
```

## 🔄 IdP Configuration

### Common IdP Configuration Steps

1. **Upload Metadata**: Provide your SP metadata to the IdP administrator
2. **Configure Entity ID**: Ensure the IdP recognizes your entity ID
3. **Set ACS URL**: Configure the Assertion Consumer Service URL
4. **Configure Attributes**: Map IdP attributes to your requested attributes
5. **Test Connection**: Verify the SAML connection works

### IdP-Specific Instructions

#### Okta
1. Go to Applications → Add Application → Create New App
2. Choose SAML 2.0
3. Upload your SP metadata or configure manually
4. Set ACS URL: `https://your-domain.com/saml/acs`
5. Set Entity ID: `https://your-domain.com/saml/metadata`

#### Azure AD
1. Go to Enterprise Applications → New Application
2. Choose "Create your own application"
3. Select "Integrate any other application you don't find in the gallery"
4. Configure SAML settings with your metadata

#### ADFS
1. Add Relying Party Trust
2. Import your SP metadata
3. Configure claim rules
4. Set ACS URL and Entity ID

## 🧪 Testing

### 1. Validate Metadata
```bash
# Check if metadata is accessible
curl -s https://your-domain.com/saml/metadata | xmllint --format -

# Validate XML structure
xmllint --noout --schema saml-schema-metadata-2.0.xsd saml-metadata.xml
```

### 2. Test SAML Flow
1. Access your application
2. Initiate SAML login
3. Verify redirect to IdP
4. Complete authentication
5. Verify response processing

### 3. Check Logs
Monitor application logs for:
- Metadata access
- SAML request/response processing
- Validation errors
- Security events

## 🚨 Common Issues

### 1. Metadata Not Accessible
- **Issue**: 404 error when accessing metadata URL
- **Solution**: Ensure static resources are properly configured

### 2. Certificate Issues
- **Issue**: Invalid certificate in metadata
- **Solution**: Verify certificate format and content

### 3. URL Mismatches
- **Issue**: IdP can't reach your endpoints
- **Solution**: Ensure URLs are correct and accessible

### 4. Attribute Mapping
- **Issue**: Attributes not received from IdP
- **Solution**: Verify attribute names and formats match

## 📊 Monitoring

### Metadata Access Metrics
- Monitor metadata endpoint access
- Track metadata download frequency
- Alert on metadata access failures

### SAML Flow Metrics
- Track successful/failed authentications
- Monitor response processing times
- Alert on validation failures

## 🔄 Maintenance

### Regular Tasks
1. **Certificate Renewal**: Monitor certificate expiration dates
2. **Metadata Updates**: Update metadata when endpoints change
3. **Security Review**: Regularly review security settings
4. **Testing**: Test SAML flows after changes

### Version Control
- Keep metadata files in version control
- Document changes and reasons
- Maintain backup copies

---

This metadata configuration provides a solid foundation for SAML integration with various Identity Providers while maintaining security best practices.
