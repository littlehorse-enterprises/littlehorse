using Microsoft.Extensions.Logging;

namespace LittleHorse.Canary.Worker;

public static class ShutdownHook
{
    private static ILogger? _logger;
    public static ILogger Logger { set => _logger = value; }

    private static bool _shuttingDown;

    public static void Add(MetronomeWorker worker)
    {
        void CloseWorker()
        {
            if (_shuttingDown) return;

            _logger?.LogInformation("Stopping Metronome Worker");
            _shuttingDown = true;
            worker.Close();
        }

        Console.CancelKeyPress += (_, _) => CloseWorker();
        AppDomain.CurrentDomain.ProcessExit += (_, _) => CloseWorker();
    }
}
