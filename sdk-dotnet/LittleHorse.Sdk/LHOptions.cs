﻿namespace LittleHorse.Common.Configuration.Models
{
    public class LHOptions
    {
        private static string GenerateClientId()
        {
            return "client-" + Guid.NewGuid().ToString().Replace("-", "");
        }

        public string LHC_API_HOST { get; set; } = "localhost";
        public int LHC_API_PORT { get; set; } = 2023;
        public string LHC_API_PROTOCOL { get; set; } = "PLAIN";
        public string LHC_CLIENT_ID { get; set; } = GenerateClientId();
        public string? LHC_CA_CERT { get; set; }
        public string? LHC_CLIENT_CERT { get; set; }
        public string? LHC_CLIENT_KEY { get; set; }
        public string? LHC_OAUTH_CLIENT_ID { get; set; }
        public string? LHC_OAUTH_CLIENT_SECRET { get; set; }
        public string? LHC_OAUTH_ACCESS_TOKEN_URL { get; set; }
        public string LHW_SERVER_CONNECT_LISTENER { get; set; } = "PLAIN";
        public int LHW_NUM_WORKER_THREADS { get; set; } = 8;
        public string LHW_TASK_WORKER_VERSION { get; set; } = string.Empty;
    }
}
