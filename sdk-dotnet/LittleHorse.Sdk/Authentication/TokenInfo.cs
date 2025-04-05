namespace LittleHorse.Sdk.Authentication
{
    internal class TokenInfo
    {
        internal string ClientId { get; init; }
        internal string AccessToken { get; init; }
        internal DateTime Expiration { get; init; }

        public bool IsExpired
        {
            get
            {
                return Expiration > DateTime.UtcNow;
            }
        }

        public TokenInfo(string clientId, string accessToken, DateTime expiration)
        {
            ClientId = clientId;
            AccessToken = accessToken;
            Expiration = expiration;
        }
    }
}
