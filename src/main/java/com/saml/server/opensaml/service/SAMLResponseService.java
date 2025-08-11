package com.saml.server.opensaml.service;

import com.saml.server.opensaml.config.SAMLProperties;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.io.MarshallingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SAMLResponseService {

    @Autowired
    private SAMLProperties samlProperties;

    @Autowired
    private SAMLUtilityService utilityService;

    @Autowired
    private SAMLSecurityService securityService;

    /**
     * Process SAML Response (POST binding)
     */
    public SAMLResponseResult processSAMLResponse(String samlResponse, String relayState) throws Exception {
        // Decode SAML response
        String decodedResponse = new String(Base64.getDecoder().decode(samlResponse));
        
        // Unmarshall the SAML response
        Response response = unmarshallSAMLResponse(decodedResponse);
        
        // Validate security
        if (!validateResponseSecurity(response)) {
            return new SAMLResponseResult(false, "Security validation failed", null, relayState, new HashMap<>());
        }
        
        // Extract user information
        String userName = extractUserName(response);
        Map<String, String> attributes = extractAttributes(response);
        
        // Create session
        String sessionId = securityService.createSession(userName, response.getID(), attributes);
        
        return new SAMLResponseResult(true, "SAML response processed successfully", userName, relayState, attributes);
    }

    /**
     * Process SAML Response (Redirect binding)
     */
    public SAMLResponseResult processSAMLResponseRedirect(String samlResponse, String relayState) throws Exception {
        // Decompress and decode SAML response
        String decodedResponse = utilityService.decodeAndDecompress(samlResponse);
        
        // Unmarshall the SAML response
        Response response = unmarshallSAMLResponse(decodedResponse);
        
        // Validate security
        if (!validateResponseSecurity(response)) {
            return new SAMLResponseResult(false, "Security validation failed", null, relayState, new HashMap<>());
        }
        
        // Extract user information
        String userName = extractUserName(response);
        Map<String, String> attributes = extractAttributes(response);
        
        // Create session
        String sessionId = securityService.createSession(userName, response.getID(), attributes);
        
        return new SAMLResponseResult(true, "SAML response processed successfully", userName, relayState, attributes);
    }

    /**
     * Unmarshall SAML Response from XML string
     */
    private Response unmarshallSAMLResponse(String xmlString) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xmlString.getBytes()));
        Element element = document.getDocumentElement();
        
        return (Response) XMLObjectSupport.getUnmarshaller(element).unmarshall(element);
    }

    /**
     * Validate response security
     */
    private boolean validateResponseSecurity(Response response) {
        try {
            // Validate response status
            if (response.getStatus() == null || response.getStatus().getStatusCode() == null) {
                return false;
            }
            
            String statusCode = response.getStatus().getStatusCode().getValue();
            if (!StatusCode.SUCCESS.equals(statusCode)) {
                return false;
            }
            
            // Validate issuer
            if (response.getIssuer() == null || !samlProperties.getIdpEntityId().equals(response.getIssuer().getValue())) {
                return false;
            }
            
            // Validate destination
            if (response.getDestination() == null || !samlProperties.getAssertionConsumerServiceURL().equals(response.getDestination())) {
                return false;
            }
            
            // Validate assertions
            List<Assertion> assertions = response.getAssertions();
            if (assertions == null || assertions.isEmpty()) {
                return false;
            }
            
            // Validate each assertion
            for (Assertion assertion : assertions) {
                if (!validateAssertion(assertion)) {
                    return false;
                }
            }
            
            // Validate signature if present
            if (response.getSignature() != null) {
                if (!validateSignature(response.getSignature())) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate SAML Assertion
     */
    private boolean validateAssertion(Assertion assertion) {
        try {
            // Validate issuer
            if (assertion.getIssuer() == null || !samlProperties.getIdpEntityId().equals(assertion.getIssuer().getValue())) {
                return false;
            }
            
            // Validate subject
            if (assertion.getSubject() == null || assertion.getSubject().getNameID() == null) {
                return false;
            }
            
            // Validate conditions
            if (assertion.getConditions() != null) {
                if (!validateConditions(assertion.getConditions())) {
                    return false;
                }
            }
            
            // Validate signature if present
            if (assertion.getSignature() != null) {
                if (!validateSignature(assertion.getSignature())) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate SAML Conditions
     */
    private boolean validateConditions(Conditions conditions) {
        try {
            // Validate NotBefore
            if (conditions.getNotBefore() != null) {
                if (System.currentTimeMillis() < conditions.getNotBefore().getMillis()) {
                    return false;
                }
            }
            
            // Validate NotOnOrAfter
            if (conditions.getNotOnOrAfter() != null) {
                if (System.currentTimeMillis() >= conditions.getNotOnOrAfter().getMillis()) {
                    return false;
                }
            }
            
            // Validate audience restriction
            if (conditions.getAudienceRestrictions() != null && !conditions.getAudienceRestrictions().isEmpty()) {
                boolean validAudience = false;
                for (AudienceRestriction restriction : conditions.getAudienceRestrictions()) {
                    for (Audience audience : restriction.getAudiences()) {
                        if (samlProperties.getEntityId().equals(audience.getAudienceURI())) {
                            validAudience = true;
                            break;
                        }
                    }
                }
                if (!validAudience) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate XML Signature
     */
    private boolean validateSignature(Signature signature) {
        try {
            // Get IdP certificate
            String idpCertificate = samlProperties.getIdpX509Certificate();
            if (idpCertificate == null || idpCertificate.trim().isEmpty()) {
                // If no certificate configured, skip signature validation
                return true;
            }
            
            // Create certificate from string
            java.security.cert.X509Certificate cert = utilityService.createCertificateFromString(idpCertificate);
            
            // Create credential and validate signature
            org.opensaml.security.credential.Credential credential = utilityService.createCredentialFromCertificate(cert);
            SignatureValidator.validate(signature, credential);
            return true;
        } catch (SignatureException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract user name from SAML response
     */
    private String extractUserName(Response response) {
        try {
            List<Assertion> assertions = response.getAssertions();
            if (assertions != null && !assertions.isEmpty()) {
                Assertion assertion = assertions.get(0);
                if (assertion.getSubject() != null && assertion.getSubject().getNameID() != null) {
                    return assertion.getSubject().getNameID().getValue();
                }
            }
            return "unknown@example.com";
        } catch (Exception e) {
            return "unknown@example.com";
        }
    }

    /**
     * Extract attributes from SAML response
     */
    private Map<String, String> extractAttributes(Response response) {
        Map<String, String> attributes = new HashMap<>();
        
        try {
            List<Assertion> assertions = response.getAssertions();
            if (assertions != null && !assertions.isEmpty()) {
                Assertion assertion = assertions.get(0);
                
                // Extract attributes from AttributeStatement
                if (assertion.getAttributeStatements() != null) {
                    for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
                        for (Attribute attribute : attributeStatement.getAttributes()) {
                            String attributeName = attribute.getName();
                            String attributeValue = "";
                            
                            // Get first attribute value
                            if (attribute.getAttributeValues() != null && !attribute.getAttributeValues().isEmpty()) {
                                org.opensaml.core.xml.XMLObject value = attribute.getAttributeValues().get(0);
                                if (value instanceof org.opensaml.saml.saml2.core.AttributeValue) {
                                    org.opensaml.saml.saml2.core.AttributeValue attrValue = (org.opensaml.saml.saml2.core.AttributeValue) value;
                                    // Extract text content from DOM element
                                    if (attrValue.getDOM() != null) {
                                        attributeValue = attrValue.getDOM().getTextContent();
                                    }
                                }
                            }
                            
                            attributes.put(attributeName, attributeValue);
                        }
                    }
                }
                
                // Extract common attributes from NameID
                if (assertion.getSubject() != null && assertion.getSubject().getNameID() != null) {
                    String nameId = assertion.getSubject().getNameID().getValue();
                    String nameIdFormat = assertion.getSubject().getNameID().getFormat();
                    
                    if (nameIdFormat != null && nameIdFormat.contains("email")) {
                        attributes.put("email", nameId);
                    }
                    attributes.put("nameId", nameId);
                    attributes.put("nameIdFormat", nameIdFormat != null ? nameIdFormat : "");
                }
            }
        } catch (Exception e) {
            // Add default attributes if extraction fails
            attributes.put("email", "user@example.com");
            attributes.put("firstName", "Unknown");
            attributes.put("lastName", "User");
        }
        
        return attributes;
    }

    /**
     * SAML Response Result class
     */
    public static class SAMLResponseResult {
        private final boolean success;
        private final String message;
        private final String userName;
        private final String relayState;
        private final Map<String, String> attributes;

        public SAMLResponseResult(boolean success, String message, String userName, String relayState, Map<String, String> attributes) {
            this.success = success;
            this.message = message;
            this.userName = userName;
            this.relayState = relayState;
            this.attributes = attributes;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getUserName() { return userName; }
        public String getRelayState() { return relayState; }
        public Map<String, String> getAttributes() { return attributes; }
    }
}
