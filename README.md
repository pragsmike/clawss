# clawss

Clojure wrapper for web-service security libraries.

- [spring-ws wss4j](http://docs.spring.io/spring-ws/site/reference/html/security.html#security-xws-security-interceptor)
- [XWSS Policy File doc](http://docs.oracle.com/cd/E17802_01/webservices/webservices/docs/1.6/tutorial/doc/XWS-SecurityIntro4.html#wp564887)

## Future plans

This package generates its own SAML Assertion.  However, [XWSS does generate SAML Assertions](http://docs.oracle.com/cd/E17802_01/webservices/webservices/docs/1.6/tutorial/doc/XWS-SecurityIntro4.html#wp580723)
if specified in the policy.
Look into letting XWSS do it.

## Standalone HTTP proxy

The HTTP proxy listens for HTTP requests (not HTTPS!) on whatever port you specify.
When your client code sends an HTTP request to it, the proxy encapsulates
that request into a secured request and sends it to the remote server.

### SAML Assertions

clawss adds a sender-vouches assertion to the Security header in outbound requests.
This is intended to identify the end user on whose behalf the request is being made.

You need to specify two things:
- nameid
- nameid-format



### Keystores

You need to specify the keystores that will be used for 
    - signing requests and verifying signature on responses
    - Two-way SSL connections

The same keystores will be used for both.

In ~/.lein/profiles.clj do something like this:

```clojure
{
 :user {
        :env {
              :trust-store "~/.ssh/truststore.jks"
              :trust-store-pass "secure"
              :insecure?        true

              :keystore "~/.ssh/my-identity.p12"
              :keystore-pass "secure"
              :keystore-type "pkcs12"

              :remote-uri-base "https://target.example.com"
              }
        }
}
```


# References:

- [Java XML Digital Signature](http://docs.oracle.com/javase/8/docs/technotes/guides/security/xmldsig/XMLDigitalSignature.html)
- [JAX-WS 2.1.1](https://jax-ws.java.net/nonav/2.1.1/docs/UsersGuide.html)
