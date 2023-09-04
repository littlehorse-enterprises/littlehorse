using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace LittleHorse.Common.Exceptions
{
    public class LHSerdeException : Exception
    {
        public LHSerdeException() : base() { }

        public LHSerdeException(string message) : base(message) { }

        public LHSerdeException(string message, Exception innerException) : base(message, innerException) { }

    }
}
