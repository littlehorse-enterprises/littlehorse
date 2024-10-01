namespace LittleHorse.Sdk.Authentication
{
    public class TokenInfo
    {
        public string ClientId { get; init; }
        public string AccessToken { get; init; }
        public DateTime Expiration { get; init; }

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
