using IdentityModel.Client;
using LittleHorse.Common.Authentication.Model;
using LittleHorse.Common.Exceptions;
using Microsoft.Extensions.Logging;
using System.Net;

namespace LittleHorse.Common.Authentication
{
    public class OAuthClient
    {
        private OAuthConfig _oAuthConfig;
        private ILogger? _logger;

        public OAuthClient(OAuthConfig oAuthConfig, ILogger? logger)
        {
            _oAuthConfig = oAuthConfig;
            _logger = logger;
        }

        public async Task<TokenInfo> GetAccessTokenAsync()
        {
            try
            {
                var client = new HttpClient();

                var response = await client.RequestClientCredentialsTokenAsync(new ClientCredentialsTokenRequest
                {
                    Address = _oAuthConfig.TokenEndpointURI.AbsoluteUri,
                    ClientId = _oAuthConfig.ClientId,
                    ClientSecret = _oAuthConfig.ClientSecret
                });

                if (response.HttpStatusCode != HttpStatusCode.OK || response.AccessToken is null)
                {
                    var errorMessage = "";
                    if (response.HttpErrorReason != null)
                    {
                        errorMessage = $"Error getting the token status: {response.HttpErrorReason}";
                    }
                    else
                    {
                        errorMessage = "Error getting the token status.";
                    }

                    throw new LHAuthorizationServerException(response.HttpStatusCode, errorMessage);
                }

                var currentDateTime = DateTime.UtcNow;

                var expirationDateTime = currentDateTime.AddSeconds(response.ExpiresIn);

                return new TokenInfo(_oAuthConfig.ClientId, response.AccessToken, expirationDateTime);
            }
            catch (Exception ex)
            {
                _logger?.LogError(ex, ex.Message);
                throw;
            }
        }
    }
}
