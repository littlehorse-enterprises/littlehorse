using System.Collections.Generic;
using Xunit;

namespace LittleHorse.Sdk.Tests;

public class LHConfigTest
{
    [Fact]
    public void ResourceExhaustedRetryEnabled_ShouldDefaultToTrue()
    {
        var config = new LHConfig(new Dictionary<string, string>());

        Assert.True(config.ResourceExhaustedRetryEnabled);
    }

    [Fact]
    public void ResourceExhaustedRetryEnabled_ShouldAllowFalse()
    {
        var config = new LHConfig(new Dictionary<string, string>
        {
            { "LHC_GRPC_RESOURCE_EXHAUSTED_RETRY", "false" }
        });

        Assert.False(config.ResourceExhaustedRetryEnabled);
    }
}
