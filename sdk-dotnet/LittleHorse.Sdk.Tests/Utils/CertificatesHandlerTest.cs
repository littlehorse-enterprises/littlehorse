using System;
using System.IO;
using LittleHorse.Sdk.Utils;
using Xunit;

namespace LittleHorse.Sdk.Tests.Utils;

public class CertificatesHandlerTest
{
    [Fact]
    public void CertManager_WithCaCertificateTrusted_ShouldReturnHttpHandler()
    {
        const string caFilename = "trusted_ca.crt";
        string caCertPath = Path.Combine(Directory.GetCurrentDirectory(), "Resources", caFilename);
        
        var currentHttpHandler = CertificatesHandler.GetHttpHandlerFrom(caCertPath);
        
        Assert.NotNull(currentHttpHandler);
    }
    
    [Fact]
    public void CertManager_WithoutCaCertificate_ShouldThrowFileNotFoundException()
    {
        const string caCertPath = "file_not_found.crt";
        var exception = Assert.Throws<FileNotFoundException>(() => CertificatesHandler.GetHttpHandlerFrom(caCertPath));

        Assert.Contains($"Certificate file {caCertPath} does not exist.", exception.Message);
    }
    
    [Fact]
    public void CertManager_WithEmptyCaCertificate_ShouldThrowException()
    {
        const string caFilename = "empty_ca.crt";
        string caCertPath = Path.Combine(Directory.GetCurrentDirectory(), "Resources", caFilename);
        
        var exception = Assert.Throws<Exception>(() => CertificatesHandler.GetHttpHandlerFrom(caCertPath));
        
        Assert.Contains($"Certificate file {caCertPath} has corrupted data.", exception.Message);
    }
}