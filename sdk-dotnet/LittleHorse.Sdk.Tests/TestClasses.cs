using System.Collections.Generic;

namespace LittleHorse.Sdk.Tests;

public class Car
{
    public int Id { get; set; }
    public float Cost { get; set; }
}

public class Person
{
    public string FirstName { get; set; }
    public int Age { get; set; }
    public List<Car> Cars { get; set; }
}