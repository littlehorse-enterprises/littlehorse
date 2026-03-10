# Tenant-Level Quotas in LH Server

This proposal aims to support rudimentary quotas for write requests at the `Tenant` level. The design is as follows:

* Quotas are based on requests which result in writing a `Command` to kafka.
* Each `Tenant` is configured with a quota amount on a _per-server_ basis, similar to how Apache Kafka provides quotas on a per-broker basis.
  * If operators wish to approximate cluster-wide quotas, they should give per-server quotas equivalent to the desired cluster-wide quota divided by the number of servers.

## Public API's

### Protobuf

```proto
message Tenant {
    // ...

    optional TenantQuotaConfig per_server_quotas = 4;
}

// Controls quotas for a Tenant
message TenantQuotasConfig {
    // This field controls the number of write requests that each Server will allow
    // for this Tenant each second.
    write_requests_per_second = 1;
}
```

### Quotas Exceeded Response

When a quota is exceeded



## Implementation Details

## Future Work

Future work (out of scope for this proposal) will allow:

* Per-Principal quotas both within and across tenants. It will use similar enforcement mechanisms.
* Cluster-wide quotas or 
