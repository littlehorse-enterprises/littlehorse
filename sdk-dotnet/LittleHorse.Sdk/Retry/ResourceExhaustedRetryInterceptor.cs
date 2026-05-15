using Google.Protobuf.WellKnownTypes;
using Google.Rpc;
using Grpc.Core;
using Grpc.Core.Interceptors;
using GrpcStatus = Grpc.Core.Status;

namespace LittleHorse.Sdk;

internal class ResourceExhaustedRetryInterceptor : Interceptor
{
    internal const string StatusDetailsKey = "grpc-status-details-bin";

    public override TResponse BlockingUnaryCall<TRequest, TResponse>(
        TRequest request,
        ClientInterceptorContext<TRequest, TResponse> context,
        BlockingUnaryCallContinuation<TRequest, TResponse> continuation)
    {
        while (true)
        {
            try
            {
                return continuation(request, context);
            }
            catch (RpcException ex) when (TryGetRetryDelay(ex, out var retryDelay))
            {
                WaitForRetryDelay(retryDelay, context.Options.CancellationToken);
            }
        }
    }

    public override AsyncUnaryCall<TResponse> AsyncUnaryCall<TRequest, TResponse>(
        TRequest request,
        ClientInterceptorContext<TRequest, TResponse> context,
        AsyncUnaryCallContinuation<TRequest, TResponse> continuation)
    {
        var state = new AsyncUnaryCallState<TResponse>();
        return new AsyncUnaryCall<TResponse>(
            RetryUnaryCallAsync(request, context, continuation, state),
            state.GetResponseHeadersAsync(),
            state.GetStatus,
            state.GetTrailers,
            state.Dispose);
    }

    internal static bool TryGetRetryDelay(RpcException exception, out TimeSpan retryDelay)
    {
        retryDelay = TimeSpan.Zero;
        if (exception.StatusCode != StatusCode.ResourceExhausted)
        {
            return false;
        }

        byte[]? statusDetailsBytes = exception.Trailers.GetValueBytes(StatusDetailsKey);
        if (statusDetailsBytes is null)
        {
            return false;
        }

        var statusDetails = Google.Rpc.Status.Parser.ParseFrom(statusDetailsBytes);
        foreach (var detail in statusDetails.Details)
        {
            if (!detail.Is(RetryInfo.Descriptor))
            {
                continue;
            }

            var retryInfo = detail.Unpack<RetryInfo>();
            if (retryInfo.RetryDelay is null)
            {
                return false;
            }

            retryDelay = retryInfo.RetryDelay.ToTimeSpan();
            return retryDelay > TimeSpan.Zero;
        }

        return false;
    }

    private static void WaitForRetryDelay(TimeSpan retryDelay, CancellationToken cancellationToken)
    {
        try
        {
            Task.Delay(retryDelay, cancellationToken).GetAwaiter().GetResult();
        }
        catch (OperationCanceledException exception)
        {
            throw new RpcException(new GrpcStatus(StatusCode.Cancelled, exception.Message));
        }
    }

    private static async Task WaitForRetryDelayAsync(TimeSpan retryDelay, CancellationToken cancellationToken)
    {
        try
        {
            await Task.Delay(retryDelay, cancellationToken);
        }
        catch (OperationCanceledException exception)
        {
            throw new RpcException(new GrpcStatus(StatusCode.Cancelled, exception.Message));
        }
    }

    private static async Task<TResponse> RetryUnaryCallAsync<TRequest, TResponse>(
        TRequest request,
        ClientInterceptorContext<TRequest, TResponse> context,
        AsyncUnaryCallContinuation<TRequest, TResponse> continuation,
        AsyncUnaryCallState<TResponse> state)
        where TRequest : class
        where TResponse : class
    {
        while (true)
        {
            var call = continuation(request, context);
            state.Track(call);

            try
            {
                var response = await call.ResponseAsync.ConfigureAwait(false);
                state.Complete(call);
                return response;
            }
            catch (RpcException ex) when (TryGetRetryDelay(ex, out var retryDelay))
            {
                call.Dispose();
                await WaitForRetryDelayAsync(retryDelay, context.Options.CancellationToken).ConfigureAwait(false);
            }
            catch
            {
                state.Complete(call);
                throw;
            }
        }
    }

    private sealed class AsyncUnaryCallState<TResponse>
    {
        private readonly object _lock = new();
        private readonly TaskCompletionSource<AsyncUnaryCall<TResponse>> _finalCall =
            new(TaskCreationOptions.RunContinuationsAsynchronously);
        private AsyncUnaryCall<TResponse>? _currentCall;

        internal void Track(AsyncUnaryCall<TResponse> call)
        {
            lock (_lock)
            {
                _currentCall = call;
            }
        }

        internal void Complete(AsyncUnaryCall<TResponse> call)
        {
            lock (_lock)
            {
                _currentCall = call;
            }
            _finalCall.TrySetResult(call);
        }

        internal async Task<Metadata> GetResponseHeadersAsync()
        {
            return await (await _finalCall.Task.ConfigureAwait(false)).ResponseHeadersAsync.ConfigureAwait(false);
        }

        internal GrpcStatus GetStatus()
        {
            if (!_finalCall.Task.IsCompletedSuccessfully)
            {
                throw new InvalidOperationException("Call has not completed yet.");
            }

            return _finalCall.Task.Result.GetStatus();
        }

        internal Metadata GetTrailers()
        {
            if (!_finalCall.Task.IsCompletedSuccessfully)
            {
                throw new InvalidOperationException("Call has not completed yet.");
            }

            return _finalCall.Task.Result.GetTrailers();
        }

        internal void Dispose()
        {
            lock (_lock)
            {
                _currentCall?.Dispose();
            }
        }
    }
}
