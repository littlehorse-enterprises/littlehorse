using System.Net.Security;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System.Security.Authentication;
using System.Security.Cryptography.X509Certificates;
using LittleHorse.Sdk.Internal;
using Microsoft.Extensions.Logging;

[assembly: InternalsVisibleTo("LittleHorse.Sdk.Tests")]

namespace LittleHorse.Sdk.Utils { 
    internal class CertificatesHandler
    {
        internal static HttpClientHandler GetHttpHandlerFrom(string pathCaCert)
        {
            var _logger = LHLoggerFactoryProvider.GetLogger<CertificatesHandler>();
            try
            {
                var caCert = new X509Certificate2(File.ReadAllBytes(pathCaCert));
                var handler = new HttpClientHandler();

                var resultCallback = handler.ServerCertificateCustomValidationCallback =
                    (_, cert, certChain, _) =>
                    {
                        if (certChain != null)
                        {
                            _logger.LogCritical($"Cert chain is different to null. Certificate chain is {certChain}");
                            certChain.ChainPolicy.TrustMode = X509ChainTrustMode.CustomRootTrust;
                            certChain.ChainPolicy.CustomTrustStore.Add(caCert);
                            certChain.ChainPolicy.ExtraStore.Add(caCert);
                        }
                        _logger.LogCritical($"Cert chain {certChain}");
                        var certChainBuilder = certChain!.Build(cert);
                        _logger.LogCritical($"Cert chain builder {certChain}");
                        
                        return certChainBuilder;
                    };
                
                _logger.LogCritical($"Cert chain {resultCallback}");

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