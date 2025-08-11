#!/bin/bash

# Production SAML Certificate Generation Script
# This script generates X.509 certificates for SAML Service Provider

set -e

# Configuration
CERT_DIR="certs"
KEYSTORE_DIR="keystore"
CERT_VALIDITY_DAYS=365
KEY_SIZE=2048
SIGNATURE_ALGORITHM="SHA256withRSA"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== SAML Certificate Generation Script ===${NC}"

# Create directories
mkdir -p $CERT_DIR
mkdir -p $KEYSTORE_DIR

# Generate private key
echo -e "${YELLOW}Generating private key...${NC}"
openssl genrsa -out $CERT_DIR/sp-private-key.pem $KEY_SIZE

# Generate certificate signing request (CSR)
echo -e "${YELLOW}Generating certificate signing request...${NC}"
openssl req -new -key $CERT_DIR/sp-private-key.pem -out $CERT_DIR/sp-certificate.csr -subj "/C=US/ST=State/L=City/O=Organization/OU=IT/CN=localhost"

# Generate self-signed certificate (for testing - use CA-signed for production)
echo -e "${YELLOW}Generating self-signed certificate...${NC}"
openssl x509 -req -in $CERT_DIR/sp-certificate.csr -signkey $CERT_DIR/sp-private-key.pem -out $CERT_DIR/sp-certificate.pem -days $CERT_VALIDITY_DAYS -sha256

# Create PKCS12 keystore
echo -e "${YELLOW}Creating PKCS12 keystore...${NC}"
openssl pkcs12 -export -in $CERT_DIR/sp-certificate.pem -inkey $CERT_DIR/sp-private-key.pem -out $KEYSTORE_DIR/sp-keystore.p12 -name "saml-sp" -passout pass:changeit

# Create JKS keystore (if needed)
echo -e "${YELLOW}Creating JKS keystore...${NC}"
keytool -importkeystore -srckeystore $KEYSTORE_DIR/sp-keystore.p12 -srcstoretype PKCS12 -destkeystore $KEYSTORE_DIR/sp-keystore.jks -deststoretype JKS -srcstorepass changeit -deststorepass changeit

# Extract certificate in base64 format for configuration
echo -e "${YELLOW}Extracting base64 certificate...${NC}"
openssl x509 -in $CERT_DIR/sp-certificate.pem -outform DER | base64 > $CERT_DIR/sp-certificate-base64.txt

# Extract private key in base64 format for configuration
echo -e "${YELLOW}Extracting base64 private key...${NC}"
openssl rsa -in $CERT_DIR/sp-private-key.pem -outform DER | base64 > $CERT_DIR/sp-private-key-base64.txt

# Set proper permissions
chmod 600 $CERT_DIR/sp-private-key.pem
chmod 600 $KEYSTORE_DIR/sp-keystore.p12
chmod 600 $KEYSTORE_DIR/sp-keystore.jks

echo -e "${GREEN}=== Certificate Generation Complete ===${NC}"
echo -e "${GREEN}Files generated:${NC}"
echo -e "  Private Key: $CERT_DIR/sp-private-key.pem"
echo -e "  Certificate: $CERT_DIR/sp-certificate.pem"
echo -e "  CSR: $CERT_DIR/sp-certificate.csr"
echo -e "  PKCS12 Keystore: $KEYSTORE_DIR/sp-keystore.p12"
echo -e "  JKS Keystore: $KEYSTORE_DIR/sp-keystore.jks"
echo -e "  Base64 Certificate: $CERT_DIR/sp-certificate-base64.txt"
echo -e "  Base64 Private Key: $CERT_DIR/sp-private-key-base64.txt"

echo -e "${YELLOW}=== Next Steps ===${NC}"
echo -e "1. Update application.properties with the certificate and key values"
echo -e "2. For production, use CA-signed certificates instead of self-signed"
echo -e "3. Store certificates securely and restrict access"
echo -e "4. Configure your Identity Provider with the SP certificate"

# Display certificate information
echo -e "${YELLOW}=== Certificate Information ===${NC}"
openssl x509 -in $CERT_DIR/sp-certificate.pem -text -noout | grep -E "(Subject:|Issuer:|Not Before|Not After)"

echo -e "${GREEN}Certificate generation completed successfully!${NC}"
