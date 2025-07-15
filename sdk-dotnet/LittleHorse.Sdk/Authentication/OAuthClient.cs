using System.Net;
using System.Text.Json;
using LittleHorse.Sdk.Exceptions;

namespace LittleHorse.Sdk.Authentication
{
    internal class OAuthClient
    {
        private OAuthConfig _oAuthConfig;

        internal OAuthClient(OAuthConfig oAuthConfig)
        {
            _oAuthConfig = oAuthConfig;
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
            
            var client = new HttpClient();
            HttpResponseMessage response = await client.PostAsync(postUrl, tokenFormData);
            
            if (response.StatusCode != HttpStatusCode.OK)
            {
                var errorMessage = $"API could not retrieve access token. {response.ReasonPhrase}";

                throw new LHAuthorizationServerException(response.StatusCode, errorMessage);
            }

            return response;
        }

        private bool ValidateRequiredTokenFields(Dictionary<string, object> tokenApiFields)
        {
            return !(string.IsNullOrEmpty(tokenApiFields["access_token"].ToString())
                    && string.IsNullOrEmpty(tokenApiFields["expires_in"].ToString()));
        }
    }
}
