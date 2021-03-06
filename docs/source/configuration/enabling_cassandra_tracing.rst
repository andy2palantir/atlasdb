==========================
Enabling Cassandra Tracing
==========================

Overview
========

Sometimes in order to do deeper performance analysis of queries hitting
your cassandra cluster, you'll want to enable tracing.  To learn more 
about tracing itself check out datastax's `Request tracing in Cassandra 
1.2 <http://www.datastax.com/dev/blog/tracing-in-cassandra-1-2>`__ and `TRACING <https://docs.datastax.com/en/cql/3.3/cql/cql_reference/tracing_r.html>`__.

To enable tracing you need to provide a configuration file called 
``atlasdb_tracing.prefs`` and place it in the user.dir directory of your 
java process.

When a query is traced you will see the following line your log file:
``Traced a call to <table-name> that took <duration> ms. It will appear in system_traces with UUID=<session-id-of-trace>``

Generally it is important to note that tracing is expensive and can have 
performance implications when reading/writing to cassandra, and thus the 
use of it should be minimized.

The Prefs File
==============

The ``atlasdb_tracing.prefs`` is a standard java properties file with 
the following parameters:

.. code:: properties

    tracing_enabled: true           # self explanatory

    # the probability we trace an eligible query
    # this is a pre-filter and a good tool to use to ensure you're not tracing
    # frequently enough to incur performance degradation
    trace_probability: 1.0

    # a comma separated list of tables whose queries are eligible for tracing
    # for namespaced tables the table entry must be <namespace>.<table>
    # like "trace_probability", this is also a pre-filter
    tables_to_trace: _transactions,namespaceOne.table_7,namespaceTwo.table_3    

    # the minimum amount of time a traced query has to take to actually be logged
    # this is a post-filter and so the trace is still done (and thus still incurs
    # a performance hit) even if you do not log it
    min_duration_to_log_ms: 1000
