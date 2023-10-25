namespace LittleHorse.Common.Authentication.Model
{
    public class OAuthConfig
    {
        public string ClientId { get; init; }
        public string ClientSecret { get; init; }
        public Uri TokenEndpointURI { get; init; }

        public OAuthConfig(string? clientId, string? clientSecret, string? tokenEndpointUrl)
        {
            if (string.IsNullOrEmpty(clientId) || string.IsNullOrEmpty(clientSecret) || string.IsNullOrEmpty(tokenEndpointUrl))
            {
                throw new ArgumentNullException("OAuth configuration is missing.");
            }

            ClientId = clientId;
            ClientSecret = clientSecret;
            TokenEndpointURI = new Uri(tokenEndpointUrl);
        }
    }
}
