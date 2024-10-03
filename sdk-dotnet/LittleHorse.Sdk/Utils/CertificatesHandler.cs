using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System.Security.Cryptography.X509Certificates;

[assembly: InternalsVisibleTo("LittleHorse.Sdk.Tests")]

namespace LittleHorse.Sdk.Utils { 
    internal class CertificatesHandler
    {
        internal static HttpClientHandler GetHttpHandlerFrom(string pathCaCert)
        {
            try
            {
                var caCert = new X509Certificate2(File.ReadAllBytes(pathCaCert));
                var handler = new HttpClientHandler();

                handler.ServerCertificateCustomValidationCallback =
                    (httpRequestMessage, cert, certChain, sslPolicyErrors) =>
                    {
                        return certChain!.ChainElements.Any(element =>
                            element.Certificate.Thumbprint == caCert.Thumbprint);
                    };

                return handler;
            }
            catch (System.Security.Cryptography.CryptographicException)
            {
                throw new Exception($"Certificate file {pathCaCert} has corrupted data.");
            }
            catch (Exception ex)
            {
                throw new FileNotFoundException($"Certificate file {pathCaCert} does not exist.", 
                    ex.Message);
            }
        }

        internal static X509Certificate2 GetX509CertificateFrom(string pathPrivateCert, string pathRequestedCert)
        {
            string certificatePem = File.ReadAllText(pathRequestedCert);
            string privateKeyPem = File.ReadAllText(pathPrivateCert);
            X509Certificate2 cert = X509Certificate2.CreateFromPem(certificatePem, privateKeyPem);
            if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows))
            {
                var originalCert = cert;
                cert = new X509Certificate2(cert.Export(X509ContentType.Pkcs12));
                originalCert.Dispose();
            }

            return cert;
        }
    }
}