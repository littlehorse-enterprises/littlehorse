using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace LittleHorse.Common.Authentication.Model
{
    public class TokenInfo
    {
        public string ClientId { get; init; }
        public string Token { get; init; }
        public DateTime Expiration { get; init; }

        public bool IsExpired
        {
            get
            {
                return Expiration > DateTime.UtcNow;
            }
        }

        public TokenInfo(string clientId, string token, DateTime expiration)
        {
            ClientId = clientId;
            Token = token;
            Expiration = expiration;
        }
    }
}
