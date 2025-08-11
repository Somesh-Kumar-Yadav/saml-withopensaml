package com.saml.server.opensaml.service;

import com.saml.server.opensaml.config.SAMLProperties;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class SAMLLogoutService {

    @Autowired
    private SAMLProperties samlProperties;

    @Autowired
    private SAMLUtilityService utilityService;

    /**
     * Create a SAML LogoutRequest
     */
    public LogoutRequest createLogoutRequest(String nameId, String sessionIndex) throws Exception {
        LogoutRequest logoutRequest = (LogoutRequest) XMLObjectSupport.buildXMLObject(LogoutRequest.DEFAULT_ELEMENT_NAME);
        
        // Set basic attributes
        logoutRequest.setID(utilityService.generateSAMLId());
        logoutRequest.setIssueInstant(org.joda.time.DateTime.now());
        logoutRequest.setDestination(samlProperties.getIdpSingleLogoutServiceURL());
        logoutRequest.setIssuer(createIssuer());
        logoutRequest.setNameID(createNameID(nameId));
        
        // Add session index if provided
        if (sessionIndex != null && !sessionIndex.isEmpty()) {
            SessionIndex sessionIndexElement = (SessionIndex) XMLObjectSupport.buildXMLObject(SessionIndex.DEFAULT_ELEMENT_NAME);
            sessionIndexElement.setSessionIndex(sessionIndex);
            logoutRequest.getSessionIndexes().add(sessionIndexElement);
        }
        
        return logoutRequest;
    }

    /**
     * Create a SAML LogoutResponse
     */
    public LogoutResponse createLogoutResponse(String inResponseTo, boolean success) throws Exception {
        LogoutResponse logoutResponse = (LogoutResponse) XMLObjectSupport.buildXMLObject(LogoutResponse.DEFAULT_ELEMENT_NAME);
        
        // Set basic attributes
        logoutResponse.setID(utilityService.generateSAMLId());
        logoutResponse.setIssueInstant(org.joda.time.DateTime.now());
        logoutResponse.setDestination(samlProperties.getSingleLogoutServiceURL());
        logoutResponse.setIssuer(createIssuer());
        logoutResponse.setInResponseTo(inResponseTo);
        logoutResponse.setStatus(createStatus(success));
        
        return logoutResponse;
    }

    /**
     * Create Issuer element
     */
    private Issuer createIssuer() {
        Issuer issuer = (Issuer) XMLObjectSupport.buildXMLObject(Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue(samlProperties.getEntityId());
        return issuer;
    }

    /**
     * Create NameID element
     */
    private NameID createNameID(String nameIdValue) {
        NameID nameID = (NameID) XMLObjectSupport.buildXMLObject(NameID.DEFAULT_ELEMENT_NAME);
        nameID.setValue(nameIdValue);
        nameID.setFormat(samlProperties.getNameIdFormat());
        return nameID;
    }

    /**
     * Create Status element
     */
    private Status createStatus(boolean success) {
        Status status = (Status) XMLObjectSupport.buildXMLObject(Status.DEFAULT_ELEMENT_NAME);
        StatusCode statusCode = (StatusCode) XMLObjectSupport.buildXMLObject(StatusCode.DEFAULT_ELEMENT_NAME);
        
        if (success) {
            statusCode.setValue("urn:oasis:names:tc:SAML:2.0:status:Success");
        } else {
            statusCode.setValue("urn:oasis:names:tc:SAML:2.0:status:Responder");
        }
        
        status.setStatusCode(statusCode);
        return status;
    }

    /**
     * Create redirect URL for LogoutRequest
     */
    public String createLogoutRedirectURL(LogoutRequest logoutRequest, String relayState) throws Exception {
        String samlRequest = utilityService.serializeSAMLObject(logoutRequest);
        String encodedSAMLRequest = utilityService.compressAndEncode(samlRequest);
        
        StringBuilder url = new StringBuilder();
        url.append(samlProperties.getIdpSingleLogoutServiceURL());
        url.append("?SAMLRequest=").append(java.net.URLEncoder.encode(encodedSAMLRequest, "UTF-8"));
        
        if (relayState != null && !relayState.isEmpty()) {
            url.append("&RelayState=").append(java.net.URLEncoder.encode(relayState, "UTF-8"));
        }
        
        return url.toString();
    }

    /**
     * Create redirect URL for LogoutResponse
     */
    public String createLogoutResponseRedirectURL(LogoutResponse logoutResponse, String relayState) throws Exception {
        String samlResponse = utilityService.serializeSAMLObject(logoutResponse);
        String encodedSAMLResponse = utilityService.compressAndEncode(samlResponse);
        
        StringBuilder url = new StringBuilder();
        url.append(samlProperties.getSingleLogoutServiceURL());
        url.append("?SAMLResponse=").append(java.net.URLEncoder.encode(encodedSAMLResponse, "UTF-8"));
        
        if (relayState != null && !relayState.isEmpty()) {
            url.append("&RelayState=").append(java.net.URLEncoder.encode(relayState, "UTF-8"));
        }
        
        return url.toString();
    }

    /**
     * Process LogoutRequest
     */
    public boolean processLogoutRequest(LogoutRequest logoutRequest) {
        try {
            // Validate the logout request
            if (logoutRequest.getIssuer() == null || !samlProperties.getIdpEntityId().equals(logoutRequest.getIssuer().getValue())) {
                return false;
            }
            
            // Extract user information for logout
            String nameId = null;
            if (logoutRequest.getNameID() != null) {
                nameId = logoutRequest.getNameID().getValue();
            }
            
            // In a real implementation, you would:
            // 1. Find the user session based on nameId
            // 2. Invalidate the session
            // 3. Clear any cached user data
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Process LogoutResponse
     */
    public boolean processLogoutResponse(LogoutResponse logoutResponse) {
        try {
            // Validate the logout response
            if (logoutResponse.getIssuer() == null || !samlProperties.getIdpEntityId().equals(logoutResponse.getIssuer().getValue())) {
                return false;
            }
            
            // Check status
            Status status = logoutResponse.getStatus();
            if (status == null || status.getStatusCode() == null) {
                return false;
            }
            
            String statusCode = status.getStatusCode().getValue();
            return "urn:oasis:names:tc:SAML:2.0:status:Success".equals(statusCode);
        } catch (Exception e) {
            return false;
        }
    }
}
