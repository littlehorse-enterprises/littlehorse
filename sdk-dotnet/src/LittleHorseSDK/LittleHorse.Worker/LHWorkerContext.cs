using LittleHorseSDK.Common.proto;

namespace LittleHorse.Worker
{
    public class LHWorkerContext
    {
        private DateTime? _scheduleDateTime;
        private ScheduledTask _scheduleTask;

        public string? LogOutput { get; private set; }

        public LHWorkerContext(ScheduledTask scheduleTask, DateTime? cheduleDateTime) {
            _scheduleTask = scheduleTask;
            _scheduleDateTime = cheduleDateTime;
        }

        public void Log(object item)
        {
            if(item != null)
            {
                LogOutput += item.ToString();
            } 
            else
            {
                LogOutput += "null";
            }
        }


    }
}
