# clawss

Clojure wrapper for web-service security libraries.

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
