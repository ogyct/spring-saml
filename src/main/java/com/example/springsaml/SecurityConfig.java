package com.example.springsaml;

import lombok.extern.slf4j.Slf4j;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml.saml2.core.impl.RequestedAuthnContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml4AuthenticationRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2AuthenticationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;

import javax.xml.namespace.QName;
import java.util.List;

@Slf4j
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .saml2Login(Customizer.withDefaults())
        ;

        return http.build();
    }


    @Bean
    Saml2AuthenticationRequestResolver authenticationRequestResolver(RelyingPartyRegistrationRepository registrations) {
        // Create a resolver for the relying party registration
        RelyingPartyRegistrationResolver registrationResolver =
                new DefaultRelyingPartyRegistrationResolver(registrations);

        // Create the default OpenSAML4 authentication request resolver
        OpenSaml4AuthenticationRequestResolver authenticationRequestResolver =
                new OpenSaml4AuthenticationRequestResolver(registrationResolver);

        // Customize the SAML Request
        authenticationRequestResolver.setAuthnRequestCustomizer((context) -> {
            var authnRequest = context.getAuthnRequest();
            RequestedAuthnContext requestedAuthnContext = new RequestedAuthnContextBuilder()
                    .buildObject();
            requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);

            // Create and add AuthnContextClassRef
            AuthnContextClassRef authnContextClassRef = new AuthnContextClassRefBuilder()
                    .buildObject();
            authnContextClassRef.setURI("http://eidas.europa.eu/LoA/low");

            // Attach AuthnContextClassRef to RequestedAuthnContext
            requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);

            // Attach RequestedAuthnContext to the SAML request
            authnRequest.setRequestedAuthnContext(requestedAuthnContext);

            Extensions extensions = (Extensions) XMLObjectProviderRegistrySupport
                    .getBuilderFactory()
                    .getBuilder(Extensions.DEFAULT_ELEMENT_NAME)
                    .buildObject(Extensions.DEFAULT_ELEMENT_NAME);

            // 3️⃣ Add <eidas:SPType> (public)
            XSAny spType = createAttribute("http://eidas.europa.eu/saml-extensions", "SPType");
            spType.setTextContent("public");
            extensions.getUnknownXMLObjects().add(spType);

            // 4️⃣ Add <eidas:RequestedAttributes>
            XSAny requestedAttributes = createAttribute("http://eidas.europa.eu/saml-extensions", "RequestedAttributes");

            // Define requested attributes
            List<String> attributes = List.of(
                    "http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier",
                    "http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName",
                    "http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName",
                    "http://eidas.europa.eu/attributes/naturalperson/CurrentAddress",
                    "http://eidas.europa.eu/attributes/naturalperson/DateOfBirth",
                    "http://eidas.europa.eu/attributes/naturalperson/PlaceOfBirth",
                    "http://www.stork.gov.eu/1.0/countryCodeOfBirth",
                    "http://www.stork.gov.eu/1.0/eMail",
                    "http://www.stork.gov.eu/1.0/age",
                    "http://schemas.eidentita.cz/moris/2016/identity/claims/phonenumber",
                    "http://schemas.eidentita.cz/moris/2016/identity/claims/tradresaid",
                    "http://schemas.eidentita.cz/moris/2016/identity/claims/idtype",
                    "http://schemas.eidentita.cz/moris/2016/identity/claims/idnumber"
            );

            // Add requested attributes
            for (String attribute : attributes) {
                XSAny requestedAttribute = createRequestedAttribute(attribute, false);
                requestedAttributes.getUnknownXMLObjects().add(requestedAttribute);
            }

            // Add special RequestedAttribute with <eidas:AttributeValue>18</eidas:AttributeValue>
            XSAny ageOverAttribute = createRequestedAttribute("http://www.stork.gov.eu/1.0/isAgeOver", false);
            XSAny attributeValue = createAttribute("http://eidas.europa.eu/saml-extensions", "AttributeValue");
            attributeValue.setTextContent("18");
            ageOverAttribute.getUnknownXMLObjects().add(attributeValue);

            requestedAttributes.getUnknownXMLObjects().add(ageOverAttribute);
            extensions.getUnknownXMLObjects().add(requestedAttributes);

            // Attach Extensions to AuthnRequest
            authnRequest.setExtensions(extensions);

        });

        return authenticationRequestResolver;
    }

    private XSAny createAttribute(String namespace, String localName) {
        return new XSAnyBuilder()
                .buildObject(namespace, localName, "eidas");
    }

    /**
     * Helper method to create a RequestedAttribute element
     */
    private XSAny createRequestedAttribute(String name, boolean isRequired) {
        XSAny requestedAttribute = createAttribute("http://eidas.europa.eu/saml-extensions", "RequestedAttribute");

        // Set XML attributes
        requestedAttribute.getUnknownAttributes().put(new QName("Name"), name);
        requestedAttribute.getUnknownAttributes().put(new QName("NameFormat"), "urn:oasis:names:tc:SAML:2.0:attrname-format:uri");
        requestedAttribute.getUnknownAttributes().put(new QName("isRequired"), Boolean.toString(isRequired));

        return requestedAttribute;
    }
}
