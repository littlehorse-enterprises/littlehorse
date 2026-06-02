using System;
using Google.Protobuf;
using Google.Protobuf.WellKnownTypes;
using Google.Rpc;
using Grpc.Core;
using Xunit;
using GrpcStatus = Grpc.Core.Status;

namespace LittleHorse.Sdk.Tests.Retry;

public class ResourceExhaustedRetryInterceptorTest
{
    [Fact]
    public void ItExtractsRetryDelayFromStatusDetails()
    {
        var retryInfo = new RetryInfo
        {
            RetryDelay = Duration.FromTimeSpan(TimeSpan.FromSeconds(2))
        };
        var statusDetails = new Google.Rpc.Status
        {
            Code = (int)Code.ResourceExhausted
        };
        statusDetails.Details.Add(Any.Pack(retryInfo));

        var rpcException = new RpcException(
            new GrpcStatus(StatusCode.ResourceExhausted, "quota exceeded"),
            new Metadata { { ResourceExhaustedRetryInterceptor.StatusDetailsKey, statusDetails.ToByteArray() } });

        var didExtract = ResourceExhaustedRetryInterceptor.TryGetRetryDelay(rpcException, out var retryDelay);

        Assert.True(didExtract);
        Assert.Equal(TimeSpan.FromSeconds(2), retryDelay);
    }

    [Fact]
    public void ItIgnoresResourceExhaustedWithoutRetryInfo()
    {
        var rpcException = new RpcException(
            new GrpcStatus(StatusCode.ResourceExhausted, "quota exceeded"),
            new Metadata());

        var didExtract = ResourceExhaustedRetryInterceptor.TryGetRetryDelay(rpcException, out _);

        Assert.False(didExtract);
    }
}
