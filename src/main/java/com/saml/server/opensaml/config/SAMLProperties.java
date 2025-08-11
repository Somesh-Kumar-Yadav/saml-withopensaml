package com.saml.server.opensaml.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "saml")
public class SAMLProperties {
    
    private String entityId;
    private String assertionConsumerServiceURL;
    private String singleLogoutServiceURL;
    private String idpEntityId;
    private String idpSingleSignOnServiceURL;
    private String idpSingleLogoutServiceURL;
    private String idpX509Certificate;
    private String spX509Certificate;
    private String spPrivateKey;
    private String nameIdFormat = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private boolean wantAssertionsSigned = true;
    private boolean wantNameId = true;
    private boolean wantNameIdEncrypted = false;
    private boolean wantAssertionsEncrypted = false;
    private int assertionValidityInSeconds = 300;
    
    // Getters and Setters
    public String getEntityId() {
        return entityId;
    }
    
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
    
    public String getAssertionConsumerServiceURL() {
        return assertionConsumerServiceURL;
    }
    
    public void setAssertionConsumerServiceURL(String assertionConsumerServiceURL) {
        this.assertionConsumerServiceURL = assertionConsumerServiceURL;
    }
    
    public String getSingleLogoutServiceURL() {
        return singleLogoutServiceURL;
    }
    
    public void setSingleLogoutServiceURL(String singleLogoutServiceURL) {
        this.singleLogoutServiceURL = singleLogoutServiceURL;
    }
    
    public String getIdpEntityId() {
        return idpEntityId;
    }
    
    public void setIdpEntityId(String idpEntityId) {
        this.idpEntityId = idpEntityId;
    }
    
    public String getIdpSingleSignOnServiceURL() {
        return idpSingleSignOnServiceURL;
    }
    
    public void setIdpSingleSignOnServiceURL(String idpSingleSignOnServiceURL) {
        this.idpSingleSignOnServiceURL = idpSingleSignOnServiceURL;
    }
    
    public String getIdpSingleLogoutServiceURL() {
        return idpSingleLogoutServiceURL;
    }
    
    public void setIdpSingleLogoutServiceURL(String idpSingleLogoutServiceURL) {
        this.idpSingleLogoutServiceURL = idpSingleLogoutServiceURL;
    }
    
    public String getIdpX509Certificate() {
        return idpX509Certificate;
    }
    
    public void setIdpX509Certificate(String idpX509Certificate) {
        this.idpX509Certificate = idpX509Certificate;
    }
    
    public String getSpX509Certificate() {
        return spX509Certificate;
    }
    
    public void setSpX509Certificate(String spX509Certificate) {
        this.spX509Certificate = spX509Certificate;
    }
    
    public String getSpPrivateKey() {
        return spPrivateKey;
    }
    
    public void setSpPrivateKey(String spPrivateKey) {
        this.spPrivateKey = spPrivateKey;
    }
    
    public String getNameIdFormat() {
        return nameIdFormat;
    }
    
    public void setNameIdFormat(String nameIdFormat) {
        this.nameIdFormat = nameIdFormat;
    }
    
    public boolean isWantAssertionsSigned() {
        return wantAssertionsSigned;
    }
    
    public void setWantAssertionsSigned(boolean wantAssertionsSigned) {
        this.wantAssertionsSigned = wantAssertionsSigned;
    }
    
    public boolean isWantNameId() {
        return wantNameId;
    }
    
    public void setWantNameId(boolean wantNameId) {
        this.wantNameId = wantNameId;
    }
    
    public boolean isWantNameIdEncrypted() {
        return wantNameIdEncrypted;
    }
    
    public void setWantNameIdEncrypted(boolean wantNameIdEncrypted) {
        this.wantNameIdEncrypted = wantNameIdEncrypted;
    }
    
    public boolean isWantAssertionsEncrypted() {
        return wantAssertionsEncrypted;
    }
    
    public void setWantAssertionsEncrypted(boolean wantAssertionsEncrypted) {
        this.wantAssertionsEncrypted = wantAssertionsEncrypted;
    }
    
    public int getAssertionValidityInSeconds() {
        return assertionValidityInSeconds;
    }
    
    public void setAssertionValidityInSeconds(int assertionValidityInSeconds) {
        this.assertionValidityInSeconds = assertionValidityInSeconds;
    }
}
