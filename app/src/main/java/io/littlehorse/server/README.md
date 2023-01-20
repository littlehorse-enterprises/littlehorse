This directory is the implementation of the actual LH Server.

At one point, stressed-out overly-optimizing Colt thought it would be a good idea to make the backend pluggable so that we could change out the Kafka Streams backend for something else (eg. Cassandra, Yugabyte, our own storage, etc).

That might be overkill and may never happen, but at least it led to a separation of the DAO layer for storing and querying data (this directory) and the business logic layer for how workflows move (the 'common/model/' directory).
