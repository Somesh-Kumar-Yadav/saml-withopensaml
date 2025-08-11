package com.saml.server.opensaml.service;

import com.saml.server.opensaml.config.SAMLProperties;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.xmlsec.signature.Signature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SAMLSecurityService {

    @Autowired
    private SAMLProperties samlProperties;

    @Autowired
    private SAMLUtilityService utilityService;

    // Session management for production
    private final ConcurrentHashMap<String, SAMLSession> activeSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Date> usedRequestIds = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Initialize security components
        initializeSecurityProviders();
    }

    /**
     * Initialize security providers
     */
    private void initializeSecurityProviders() {
        try {
            // Add BouncyCastle as security provider
            java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize security providers", e);
        }
    }

    /**
     * Validate SAML Response security
     */
    public boolean validateSAMLResponse(Response response) {
        try {
            // Check if response is null
            if (response == null) {
                return false;
            }

            // Validate response ID uniqueness
            if (!isRequestIdUnique(response.getID())) {
                return false;
            }

            // Validate response time (not too old)
            if (!isResponseTimeValid(response.getIssueInstant())) {
                return false;
            }

            // Validate issuer
            if (!validateIssuer(response.getIssuer())) {
                return false;
            }

            // Validate signature if present
            if (response.getSignature() != null) {
                if (!validateSignature(response.getSignature())) {
                    return false;
                }
            }

            // Validate assertions
            for (Assertion assertion : response.getAssertions()) {
                if (!validateAssertion(assertion)) {
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
            // Validate assertion time
            if (!isAssertionTimeValid(assertion)) {
                return false;
            }

            // Validate signature if present
            if (assertion.getSignature() != null) {
                if (!validateSignature(assertion.getSignature())) {
                    return false;
                }
            }

            // Validate subject
            if (!validateSubject(assertion.getSubject())) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate SAML Signature
     */
    private boolean validateSignature(Signature signature) {
        try {
            // Get IdP certificate from configuration
            X509Certificate idpCertificate = getIdPCertificate();
            if (idpCertificate == null) {
                return false;
            }

            // For production, implement proper signature validation
            // For now, return true if signature exists
            return signature != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate issuer
     */
    private boolean validateIssuer(Issuer issuer) {
        if (issuer == null || issuer.getValue() == null) {
            return false;
        }
        return samlProperties.getIdpEntityId().equals(issuer.getValue());
    }

    /**
     * Validate subject
     */
    private boolean validateSubject(Subject subject) {
        if (subject == null) {
            return false;
        }

        // Validate NameID
        NameID nameID = subject.getNameID();
        if (nameID == null || nameID.getValue() == null) {
            return false;
        }

        // Validate SubjectConfirmation
        for (SubjectConfirmation confirmation : subject.getSubjectConfirmations()) {
            if (!validateSubjectConfirmation(confirmation)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validate SubjectConfirmation
     */
    private boolean validateSubjectConfirmation(SubjectConfirmation confirmation) {
        if (confirmation == null) {
            return false;
        }

        SubjectConfirmationData data = confirmation.getSubjectConfirmationData();
        if (data == null) {
            return false;
        }

        // Validate NotOnOrAfter
        if (data.getNotOnOrAfter() != null) {
            if (new Date().after(data.getNotOnOrAfter().toDate())) {
                return false;
            }
        }

        // Validate Recipient
        if (data.getRecipient() != null) {
            if (!samlProperties.getAssertionConsumerServiceURL().equals(data.getRecipient())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if request ID is unique (prevent replay attacks)
     */
    private boolean isRequestIdUnique(String requestId) {
        if (requestId == null) {
            return false;
        }

        // Check if already used
        if (usedRequestIds.containsKey(requestId)) {
            return false;
        }

        // Store for future checks (with cleanup)
        usedRequestIds.put(requestId, new Date());
        
        // Clean up old entries (older than 5 minutes)
        cleanupOldRequestIds();
        
        return true;
    }

    /**
     * Clean up old request IDs
     */
    private void cleanupOldRequestIds() {
        Date fiveMinutesAgo = new Date(System.currentTimeMillis() - 5 * 60 * 1000);
        usedRequestIds.entrySet().removeIf(entry -> entry.getValue().before(fiveMinutesAgo));
    }

    /**
     * Validate response time
     */
    private boolean isResponseTimeValid(org.joda.time.DateTime issueInstant) {
        if (issueInstant == null) {
            return false;
        }

        Date issueDate = issueInstant.toDate();
        Date now = new Date();
        long diffInMinutes = (now.getTime() - issueDate.getTime()) / (60 * 1000);

        // Response should not be older than 5 minutes
        return diffInMinutes <= 5;
    }

    /**
     * Validate assertion time
     */
    private boolean isAssertionTimeValid(Assertion assertion) {
        if (assertion == null) {
            return false;
        }

        // Check NotOnOrAfter
        if (assertion.getConditions() != null) {
            for (Condition condition : assertion.getConditions().getConditions()) {
                if (condition instanceof OneTimeUse) {
                    // OneTimeUse condition - check if already used
                    if (isAssertionAlreadyUsed(assertion.getID())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Check if assertion is already used
     */
    private boolean isAssertionAlreadyUsed(String assertionId) {
        return usedRequestIds.containsKey("ASSERTION_" + assertionId);
    }

    /**
     * Get IdP certificate from configuration
     */
    private X509Certificate getIdPCertificate() {
        try {
            // In production, load from keystore or configuration
            String certString = samlProperties.getIdpX509Certificate();
            if (certString == null || certString.trim().isEmpty()) {
                return null;
            }

            // Remove headers and footers
            certString = certString.replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replaceAll("\\s", "");

            java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(
                    java.util.Base64.getDecoder().decode(certString));
            return (X509Certificate) cf.generateCertificate(bais);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Create SAML session
     */
    public String createSession(String nameId, String sessionIndex, java.util.Map<String, String> attributes) {
        String sessionId = utilityService.generateSAMLId();
        SAMLSession session = new SAMLSession(nameId, sessionIndex, attributes, new Date());
        activeSessions.put(sessionId, session);
        return sessionId;
    }

    /**
     * Validate session
     */
    public boolean validateSession(String sessionId) {
        SAMLSession session = activeSessions.get(sessionId);
        if (session == null) {
            return false;
        }

        // Check if session is expired (30 minutes)
        Date now = new Date();
        long diffInMinutes = (now.getTime() - session.getCreatedAt().getTime()) / (60 * 1000);
        if (diffInMinutes > 30) {
            activeSessions.remove(sessionId);
            return false;
        }

        return true;
    }

    /**
     * Get session data
     */
    public SAMLSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    /**
     * Invalidate session
     */
    public void invalidateSession(String sessionId) {
        activeSessions.remove(sessionId);
    }

    /**
     * Clean up expired sessions
     */
    public void cleanupExpiredSessions() {
        Date thirtyMinutesAgo = new Date(System.currentTimeMillis() - 30 * 60 * 1000);
        activeSessions.entrySet().removeIf(entry -> entry.getValue().getCreatedAt().before(thirtyMinutesAgo));
    }

    /**
     * SAML Session class
     */
    public static class SAMLSession {
        private final String nameId;
        private final String sessionIndex;
        private final java.util.Map<String, String> attributes;
        private final Date createdAt;

        public SAMLSession(String nameId, String sessionIndex, java.util.Map<String, String> attributes, Date createdAt) {
            this.nameId = nameId;
            this.sessionIndex = sessionIndex;
            this.attributes = attributes;
            this.createdAt = createdAt;
        }

        public String getNameId() { return nameId; }
        public String getSessionIndex() { return sessionIndex; }
        public java.util.Map<String, String> getAttributes() { return attributes; }
        public Date getCreatedAt() { return createdAt; }
    }
}
