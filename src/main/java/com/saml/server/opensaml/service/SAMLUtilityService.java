package com.saml.server.opensaml.service;

import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

@Service
public class SAMLUtilityService {

    /**
     * Serialize a SAML object to XML string
     */
    public String serializeSAMLObject(XMLObject samlObject) throws MarshallingException {
        Element element = XMLObjectSupport.marshall(samlObject);
        return SerializeSupport.nodeToString(element);
    }

    /**
     * Compress and Base64 encode a SAML message
     */
    public String compressAndEncode(String samlMessage) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, new Deflater(Deflater.DEFLATED, true));
        deflaterOutputStream.write(samlMessage.getBytes());
        deflaterOutputStream.close();
        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
    }

    /**
     * Decode and decompress a SAML message
     */
    public String decodeAndDecompress(String encodedMessage) throws IOException {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedMessage);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodedBytes);
        InflaterInputStream inflaterInputStream = new InflaterInputStream(byteArrayInputStream, new Inflater(true));
        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inflaterInputStream.read(buffer)) > 0) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        inflaterInputStream.close();
        return byteArrayOutputStream.toString();
    }

    /**
     * Validate SAML signature
     */
    public boolean validateSignature(SignableSAMLObject signableObject) {
        try {
            Signature signature = signableObject.getSignature();
            if (signature == null) {
                return false;
            }
            SignatureValidator.validate(signature, getCredential());
            return true;
        } catch (SignatureException e) {
            return false;
        }
    }

    /**
     * Get credential for signature validation (to be implemented based on your certificate setup)
     */
    private org.opensaml.security.credential.Credential getCredential() {
        // This should be implemented to return the appropriate credential
        // based on your certificate configuration
        throw new UnsupportedOperationException("Credential provider not implemented");
    }

    /**
     * Create a unique ID for SAML elements
     */
    public String generateSAMLId() {
        return "_" + java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Get current time in SAML format
     */
    public String getCurrentTime() {
        return org.joda.time.DateTime.now().toString();
    }

    /**
     * Get time after specified seconds in SAML format
     */
    public String getTimeAfter(int seconds) {
        return org.joda.time.DateTime.now().plusSeconds(seconds).toString();
    }
}
