namespace LittleHorse.Canary.Worker;

public class ServerInfo(string host, int port)
{
    public string Host { get; } = host;
    public int Port { get; } = port;
}