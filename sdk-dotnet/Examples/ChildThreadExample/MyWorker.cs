using LittleHorse.Sdk.Worker;

namespace ChildThreadExample;

public class MyWorker
{
    [LHTaskMethod("save-request-form-data")]
    public string SaveRequestFormData() {
        Console.WriteLine("Executing task-b");
        return "hello there save-request-form-data";
    }

    [LHTaskMethod("validate-identification")]
    public string ValidateIdentification() {
        Console.WriteLine("Executing validate-identification");
        return "hello there validate-identification";
    }

    [LHTaskMethod("send-otp-by-email")]
    public string SendOtpByEmail() {
        Console.WriteLine("Executing send-otp-by-email");
        return "hello there send-otp-by-email";
    }
    
    [LHTaskMethod("send-otp-by-sms")]
    public string SendOtpBySms() {
        Console.WriteLine("Executing send-otp-by-sms");
        return "hello there send-otp-by-sms";
    }
    
    [LHTaskMethod("check-otp")]
    public string CheckOtp() {
        Console.WriteLine("Executing check-otp");
        return "hello there check-otp";
    }
    
    [LHTaskMethod("choose-option")]
    public string ChooseOption() {
        Console.WriteLine("Executing choose-option");
        return "hello there choose-option";
    }
    
    [LHTaskMethod("fill-client-information-zendesk")]
    public string FillClientInformationInZendesk() {
        Console.WriteLine("Executing fill-client-information-zendesk");
        return "hello there fill-client-information-zendesk";
    }
    
    [LHTaskMethod("create-ticket")]
    public string CreateTicket() {
        Console.WriteLine("Executing create-ticket");
        return "hello there create-ticket";
    }
}