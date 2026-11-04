namespace LittleHorse.Sdk.Authentication
{
    internal class OAuthConfig
    {
        internal string ClientId { get; init; }
        internal string ClientSecret { get; init; }
        internal Uri TokenEndpointURI { get; init; }
        internal string? CaCert { get; init; }

        internal OAuthConfig(string? clientId, string? clientSecret, string? tokenEndpointUrl, string? caCert = null)
        {
            if (string.IsNullOrEmpty(clientId) || string.IsNullOrEmpty(clientSecret) || string.IsNullOrEmpty(tokenEndpointUrl))
            {
                throw new ArgumentNullException("OAuth configuration is missing.");
            }

            ClientId = clientId;
            ClientSecret = clientSecret;
            TokenEndpointURI = new Uri(tokenEndpointUrl);
            CaCert = caCert;
        }
    }
}
