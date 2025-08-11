package com.saml.server.opensaml.service;

import com.saml.server.opensaml.config.SAMLProperties;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

@Service
public class SAMLAuthRequestService {

    @Autowired
    private SAMLProperties samlProperties;

    @Autowired
    private SAMLUtilityService utilityService;

    /**
     * Create a SAML AuthnRequest
     */
    public AuthnRequest createAuthnRequest() throws Exception {
        AuthnRequest authnRequest = (AuthnRequest) XMLObjectSupport.buildXMLObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
        
        // Set basic attributes
        authnRequest.setID(utilityService.generateSAMLId());
        authnRequest.setIssueInstant(org.joda.time.DateTime.now());
        authnRequest.setProtocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        authnRequest.setAssertionConsumerServiceURL(samlProperties.getAssertionConsumerServiceURL());
        authnRequest.setDestination(samlProperties.getIdpSingleSignOnServiceURL());
        authnRequest.setIssuer(createIssuer());
        authnRequest.setNameIDPolicy(createNameIDPolicy());
        authnRequest.setRequestedAuthnContext(createRequestedAuthnContext());
        
        return authnRequest;
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
     * Create NameIDPolicy element
     */
    private NameIDPolicy createNameIDPolicy() {
        NameIDPolicy nameIDPolicy = (NameIDPolicy) XMLObjectSupport.buildXMLObject(NameIDPolicy.DEFAULT_ELEMENT_NAME);
        nameIDPolicy.setFormat(samlProperties.getNameIdFormat());
        nameIDPolicy.setAllowCreate(true);
        return nameIDPolicy;
    }

    /**
     * Create RequestedAuthnContext element
     */
    private RequestedAuthnContext createRequestedAuthnContext() {
        RequestedAuthnContext requestedAuthnContext = (RequestedAuthnContext) XMLObjectSupport.buildXMLObject(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
        
        AuthnContextClassRef authnContextClassRef = (AuthnContextClassRef) XMLObjectSupport.buildXMLObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
        
        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);
        
        return requestedAuthnContext;
    }

    /**
     * Serialize and encode AuthnRequest for HTTP-Redirect binding
     */
    public String createRedirectURL(AuthnRequest authnRequest, String relayState) throws Exception {
        String samlRequest = utilityService.serializeSAMLObject(authnRequest);
        String encodedSAMLRequest = utilityService.compressAndEncode(samlRequest);
        
        StringBuilder url = new StringBuilder();
        url.append(samlProperties.getIdpSingleSignOnServiceURL());
        url.append("?SAMLRequest=").append(java.net.URLEncoder.encode(encodedSAMLRequest, "UTF-8"));
        
        if (relayState != null && !relayState.isEmpty()) {
            url.append("&RelayState=").append(java.net.URLEncoder.encode(relayState, "UTF-8"));
        }
        
        return url.toString();
    }

    /**
     * Create form data for HTTP-POST binding
     */
    public String createPostFormData(AuthnRequest authnRequest, String relayState) throws Exception {
        String samlRequest = utilityService.serializeSAMLObject(authnRequest);
        String encodedSAMLRequest = Base64.getEncoder().encodeToString(samlRequest.getBytes());
        
        StringBuilder formData = new StringBuilder();
        formData.append("SAMLRequest=").append(java.net.URLEncoder.encode(encodedSAMLRequest, "UTF-8"));
        
        if (relayState != null && !relayState.isEmpty()) {
            formData.append("&RelayState=").append(java.net.URLEncoder.encode(relayState, "UTF-8"));
        }
        
        return formData.toString();
    }
}
