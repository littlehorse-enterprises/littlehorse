from datetime import datetime, timedelta, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import json
import os
from pathlib import Path
import select
import socket
import socketserver
import ssl
import tempfile
import threading
from typing import Optional
import unittest
from unittest.mock import patch

from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.x509.oid import ExtendedKeyUsageOID, NameOID
import requests

from littlehorse.client_interceptors import OAuthCredentialsProvider

PROXY_VARIABLES = (
    "HTTPS_PROXY",
    "https_proxy",
    "HTTP_PROXY",
    "http_proxy",
    "ALL_PROXY",
    "all_proxy",
    "NO_PROXY",
    "no_proxy",
)


def _issue_certificates(directory: Path) -> tuple[Path, Path, Path]:
    """Writes a CA and a localhost server certificate signed by it."""
    now = datetime.now(timezone.utc)
    ca_key = ec.generate_private_key(ec.SECP256R1())
    ca_name = x509.Name(
        [x509.NameAttribute(NameOID.COMMON_NAME, "littlehorse-test-ca")]
    )
    ca_cert = (
        x509.CertificateBuilder()
        .subject_name(ca_name)
        .issuer_name(ca_name)
        .public_key(ca_key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(now - timedelta(minutes=5))
        .not_valid_after(now + timedelta(days=1))
        .add_extension(x509.BasicConstraints(ca=True, path_length=None), critical=True)
        .add_extension(
            x509.KeyUsage(
                digital_signature=False,
                content_commitment=False,
                key_encipherment=False,
                data_encipherment=False,
                key_agreement=False,
                key_cert_sign=True,
                crl_sign=True,
                encipher_only=False,
                decipher_only=False,
            ),
            critical=True,
        )
        .add_extension(
            x509.SubjectKeyIdentifier.from_public_key(ca_key.public_key()),
            critical=False,
        )
        .sign(ca_key, hashes.SHA256())
    )

    server_key = ec.generate_private_key(ec.SECP256R1())
    server_cert = (
        x509.CertificateBuilder()
        .subject_name(x509.Name([x509.NameAttribute(NameOID.COMMON_NAME, "localhost")]))
        .issuer_name(ca_name)
        .public_key(server_key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(now - timedelta(minutes=5))
        .not_valid_after(now + timedelta(days=1))
        .add_extension(
            x509.SubjectAlternativeName([x509.DNSName("localhost")]), critical=False
        )
        .add_extension(
            x509.AuthorityKeyIdentifier.from_issuer_public_key(ca_key.public_key()),
            critical=False,
        )
        .add_extension(
            x509.ExtendedKeyUsage([ExtendedKeyUsageOID.SERVER_AUTH]), critical=False
        )
        .sign(ca_key, hashes.SHA256())
    )

    ca_path = directory / "ca.crt"
    cert_path = directory / "server.crt"
    key_path = directory / "server.key"
    ca_path.write_bytes(ca_cert.public_bytes(serialization.Encoding.PEM))
    cert_path.write_bytes(server_cert.public_bytes(serialization.Encoding.PEM))
    key_path.write_bytes(
        server_key.private_bytes(
            serialization.Encoding.PEM,
            serialization.PrivateFormat.PKCS8,
            serialization.NoEncryption(),
        )
    )
    return ca_path, cert_path, key_path


class _TokenHandler(BaseHTTPRequestHandler):
    def do_POST(self) -> None:
        self.rfile.read(int(self.headers.get("Content-Length", 0)))
        body = json.dumps(
            {
                "access_token": "ca-token",
                "token_type": "Bearer",
                "expires_in": 3600,
            }
        ).encode()
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, *args: object) -> None:
        pass


class _TokenServer(ThreadingHTTPServer):
    def handle_error(self, request: object, client_address: object) -> None:
        """Clients that reject the certificate abort the connection on purpose."""


class _ConnectProxy(socketserver.ThreadingTCPServer):
    daemon_threads = True
    allow_reuse_address = True

    def __init__(self) -> None:
        super().__init__(("127.0.0.1", 0), _ConnectHandler)
        self.tunnels: list[str] = []

    def handle_error(self, request: object, client_address: object) -> None:
        """Tunnels close abruptly when the client rejects the certificate."""


class _ConnectHandler(socketserver.BaseRequestHandler):
    """Minimal HTTP CONNECT proxy that pipes bytes between client and target."""

    def handle(self) -> None:
        head = b""
        while b"\r\n\r\n" not in head:
            chunk = self.request.recv(4096)
            if not chunk:
                return
            head += chunk
        method, target, _ = head.split(b"\r\n", 1)[0].decode().split(" ", 2)
        if method != "CONNECT":
            self.request.sendall(b"HTTP/1.1 405 Method Not Allowed\r\n\r\n")
            return

        host, port = target.rsplit(":", 1)
        self.server.tunnels.append(target)  # type: ignore[attr-defined]
        with socket.create_connection((host, int(port))) as upstream:
            self.request.sendall(b"HTTP/1.1 200 Connection Established\r\n\r\n")
            peers = {self.request: upstream, upstream: self.request}
            while True:
                readable, _, _ = select.select(list(peers), [], [], 5)
                if not readable:
                    return
                for source in readable:
                    data = source.recv(65536)
                    if not data:
                        return
                    peers[source].sendall(data)


class TestOAuthCaCert(unittest.TestCase):
    def setUp(self) -> None:
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = Path(directory.name)
        self.ca_path, cert_path, key_path = _issue_certificates(self.directory)

        self.token_server = _TokenServer(("127.0.0.1", 0), _TokenHandler)
        server_context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
        server_context.load_cert_chain(cert_path, key_path)
        self.token_server.socket = server_context.wrap_socket(
            self.token_server.socket, server_side=True
        )
        self._serve(self.token_server)
        self.token_url = f"https://localhost:{self.token_server.server_port}/token"

        # Keeps the developer's proxy settings out of the tests.
        environment = patch.dict(os.environ)
        environment.start()
        self.addCleanup(environment.stop)
        for variable in PROXY_VARIABLES:
            os.environ.pop(variable, None)

    def _serve(self, server: socketserver.BaseServer) -> None:
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        self.addCleanup(server.server_close)
        self.addCleanup(server.shutdown)

    def _use_proxy(self) -> _ConnectProxy:
        proxy = _ConnectProxy()
        self._serve(proxy)
        os.environ["HTTPS_PROXY"] = f"http://127.0.0.1:{proxy.server_address[1]}"
        return proxy

    def _provider(self, ca_cert: Optional[str]) -> OAuthCredentialsProvider:
        return OAuthCredentialsProvider(
            "my-client", "my-secret", self.token_url, ca_cert
        )

    def test_fetches_token_with_ca_cert(self) -> None:
        token = self._provider(str(self.ca_path)).access_token()

        self.assertEqual("ca-token", token.token)

    def test_rejects_token_server_without_ca_cert(self) -> None:
        with self.assertRaises(requests.exceptions.SSLError):
            self._provider(None).access_token()

    def test_fails_with_invalid_ca_cert(self) -> None:
        invalid = self.directory / "invalid.crt"
        invalid.write_text("not a certificate")

        with self.assertRaises(ssl.SSLError):
            self._provider(str(invalid))

    def test_fetches_token_through_connect_proxy_with_ca_cert(self) -> None:
        proxy = self._use_proxy()

        token = self._provider(str(self.ca_path)).access_token()

        self.assertEqual("ca-token", token.token)
        self.assertEqual([f"localhost:{self.token_server.server_port}"], proxy.tunnels)

    def test_rejects_token_server_through_proxy_without_ca_cert(self) -> None:
        self._use_proxy()

        with self.assertRaises(requests.exceptions.SSLError):
            self._provider(None).access_token()


if __name__ == "__main__":
    unittest.main()
