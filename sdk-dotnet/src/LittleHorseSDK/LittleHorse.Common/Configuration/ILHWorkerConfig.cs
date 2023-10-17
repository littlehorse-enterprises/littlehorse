﻿using LittleHorseSDK.Common.proto;
using static LittleHorseSDK.Common.proto.LHPublicApi;

namespace LittleHorse.Common.Configuration
{
    public interface ILHWorkerConfig
    {
        string BootstrapHost { get; }
        int BootstrapPort { get; }
        string ClientId { get; }
        string TaskWorkerVersion { get; }
        string ConnectListener { get; }
        int WorkerThreads { get; }
        bool IsOAuth { get; }
        public LHPublicApiClient GetGrcpClientInstance();
        public LHPublicApiClient GetGrcpClientInstance(string host, int port);
        public TaskDef GetTaskDef(string taskDefName);
    }
}
