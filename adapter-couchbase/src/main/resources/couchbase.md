# Couchbase Driver

This is a driver for Couchbase.
It supports running queries using [N1QL](https://docs.couchbase.com/server/current/n1ql/n1ql-language-reference/index.html)
and direct data operations via the Java SDK.


### Example activity definitions

Run a Couchbase activity with definitions from `activities/cb-basic.yaml`

```shell
... driver=couchbase yaml=activities/cb-basic.yaml
```

### Couchbase DriverAdapter Parameters

- **connection** (Mandatory) - connection string of the Couchbase cluster

    Example: `couchbase://127.0.0.1`

- **username** (Mandatory) - username to use when authenticating with the cluster

  Example: `admin`

- **password** (Mandatory) - password to use when authenticating with the cluster

  Example: `password`

- **bucket** (Mandatory) - target bucket

    Example: `testbucket`
