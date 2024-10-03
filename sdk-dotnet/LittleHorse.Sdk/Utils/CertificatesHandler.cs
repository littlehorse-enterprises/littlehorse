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