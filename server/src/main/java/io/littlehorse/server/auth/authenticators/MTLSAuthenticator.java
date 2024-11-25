package io.littlehorse.server.auth.authenticators;

import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.server.auth.LHServerInterceptor;
import java.security.Principal;
import java.util.List;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import lombok.extern.slf4j.Slf4j;

/**
 * Authenticator for requests on MTLS listeners. Sets the principal id to the common name of the client
 * certificate.
 */
@Slf4j
public class MTLSAuthenticator implements LHServerInterceptor {

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        SSLSession clientTlsInfo = call.getAttributes().get(Grpc.TRANSPORT_ATTR_SSL_SESSION);

        if (clientTlsInfo == null) {
            throw new IllegalStateException(
                    "MTLSAuthenticator should only be used on MTLS ports, so SSLSession should be present");
        }

        String commonName;
        try {
            // Determine commonName from the client certificate
            Principal peerPrincipal = clientTlsInfo.getPeerPrincipal(); // NOT a littlehorse Principal

            LdapName ln = new LdapName(peerPrincipal.getName());

            List<String> commonNames = ln.getRdns().stream()
                    .filter(rdn -> rdn.getType().equalsIgnoreCase("CN"))
                    .map(Rdn::getValue)
                    .map(Object::toString)
                    .toList();

            if (commonNames.size() == 0) {
                // This happens when the client certificate does not have a CommonName.
                // Note that the interceptor wouldn't even be called if the SSL handshake failed,
                // so we know at this point that the client presented a valid certificate.
                //
                // Since they did not set a commonName on the certificate, we treat them as
                // anonymous.
                commonName = LHConstants.ANONYMOUS_PRINCIPAL;
            } else {
                commonName = commonNames.get(0);
            }

            headers.put(CLIENT_ID, commonName);
            log.trace("Got common name from client certificate: {}", commonName);
            return next.startCall(call, headers);
        } catch (InvalidNameException | SSLPeerUnverifiedException e) {
            // close the call as unauthenticated
            log.trace("Closing the call as unauthenticated due to certiciate exception", e);
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid certificate"), new Metadata());
            return new ServerCall.Listener<>() {};
        }
    }
}
