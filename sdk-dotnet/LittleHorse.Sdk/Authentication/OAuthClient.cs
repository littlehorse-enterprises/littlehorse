using System.Net;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;
using System.Text.Json;
using LittleHorse.Sdk.Exceptions;

namespace LittleHorse.Sdk.Authentication
{
    internal class OAuthClient
    {
        private OAuthConfig _oAuthConfig;
        private readonly X509Certificate2Collection? _caCertificates;

        internal OAuthClient(OAuthConfig oAuthConfig)
        {
            _oAuthConfig = oAuthConfig;
            if (oAuthConfig.CaCert is not null)
            {
                _caCertificates = new X509Certificate2Collection();
                _caCertificates.ImportFromPemFile(oAuthConfig.CaCert);
                if (_caCertificates.Count == 0)
                {
                    throw new ArgumentException($"No certificates found in {oAuthConfig.CaCert}");
                }
            }
        }

        internal async Task<TokenInfo> GetAccessTokenAsync()
        {
            var tokenResponseFromApi = await GetTokenResponseFromApi();
            var responseTokenApiAsString = await tokenResponseFromApi.Content.ReadAsStringAsync();

            var tokenApiFields = 
                JsonSerializer.Deserialize<Dictionary<string, object>>(responseTokenApiAsString) ?? 
                                 throw new ArgumentNullException(
                                     $"JsonSerializer.Deserialize<Dictionary<string, object>>(tokenInfo)");

            if (ValidateRequiredTokenFields(tokenApiFields))
            {
                string accessToken = tokenApiFields["access_token"].ToString()!;  
                var currentDateTime = DateTime.UtcNow;
                var expiresIn = int.Parse(tokenApiFields["expires_in"].ToString()!);
                var expirationDateTime = currentDateTime.AddSeconds(expiresIn);

                return new TokenInfo(_oAuthConfig.ClientId, accessToken, expirationDateTime);
            }

            throw new LHAuthorizationServerException("An error has occurred while getting access token information.");
        }

        private async Task<HttpResponseMessage> GetTokenResponseFromApi()
        {
            var postUrl = _oAuthConfig.TokenEndpointURI.AbsoluteUri;
            var tokenFormData = new FormUrlEncodedContent(new[]
            {
                new KeyValuePair<string, string>("client_id", _oAuthConfig.ClientId),
                new KeyValuePair<string, string>("client_secret", _oAuthConfig.ClientSecret),
                new KeyValuePair<string, string>("grant_type", "client_credentials")
            });
            
            var client = new HttpClient(CreateHandler());
            HttpResponseMessage response = await client.PostAsync(postUrl, tokenFormData);
            
            if (response.StatusCode != HttpStatusCode.OK)
            {
                var errorMessage = $"API could not retrieve access token. {response.ReasonPhrase}";

                throw new LHAuthorizationServerException(response.StatusCode, errorMessage);
            }

            return response;
        }

        private HttpClientHandler CreateHandler()
        {
            var handler = new HttpClientHandler();
            if (_caCertificates is null)
            {
                return handler;
            }

            // Accepts certificates trusted by the system or issued by the configured CAs.
            handler.ServerCertificateCustomValidationCallback = (_, certificate, _, errors) =>
            {
                if (errors == SslPolicyErrors.None)
                {
                    return true;
                }
                if (certificate is null || errors != SslPolicyErrors.RemoteCertificateChainErrors)
                {
                    return false;
                }

                using var chain = new X509Chain();
                chain.ChainPolicy.TrustMode = X509ChainTrustMode.CustomRootTrust;
                chain.ChainPolicy.CustomTrustStore.AddRange(_caCertificates);
                chain.ChainPolicy.RevocationMode = X509RevocationMode.NoCheck;
                return chain.Build(certificate);
            };
            return handler;
        }

        private bool ValidateRequiredTokenFields(Dictionary<string, object> tokenApiFields)
        {
            return !(string.IsNullOrEmpty(tokenApiFields["access_token"].ToString())
                    && string.IsNullOrEmpty(tokenApiFields["expires_in"].ToString()));
        }
    }
}
