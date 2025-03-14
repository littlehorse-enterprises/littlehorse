namespace LittleHorse.Sdk.Tests;

public static class DefaultLHConfigVariables
{ 
    public static string LHC_API_HOST => "localhost";
    public static int LHC_API_PORT => 2023;
    public static string LHC_API_PROTOCOL => "PLAINTEXT";
    public static string LHC_CLIENT_ID => "client-";
    public static int LHW_NUM_WORKER_THREADS => 8;
    public static string LHW_TASK_WORKER_VERSION => string.Empty;
}