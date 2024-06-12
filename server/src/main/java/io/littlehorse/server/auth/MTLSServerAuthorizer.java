package io.littlehorse.server.auth;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.littlehorse.common.util.CertificateUtil;
import io.littlehorse.server.listener.MTLSConfig;

public class MTLSServerAuthorizer implements ServerAuthorizer {

    private final String commonName;

    public MTLSServerAuthorizer(MTLSConfig mtlsConfig) {
        try {
            this.commonName = CertificateUtil.getCommonNameFromCertificate(mtlsConfig.getCaCertificate());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Certificate is not valid", ex);
        }
    }

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        headers.put(CLIENT_ID, this.commonName);

        return next.startCall(call, headers);

        // throw new UnsupportedOperationException("Unimplemented method
        // 'interceptCall'");
    }
}
