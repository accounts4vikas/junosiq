
CREATE KEYSPACE junosiqstreamdb
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 2};

CREATE TABLE metrics_agg_1sec (
	event_key text,
	event_day text,
	event_time timestamp,
	min_value bigint,
	max_value bigint,
	avg_value bigint,
	PRIMARY KEY ((event_key ,event_day),event_time)
) WITH CLUSTERING ORDER BY (event_time DESC);


CREATE TABLE metrics_agg_30sec (
	event_key text,
	event_day text,
	event_time timestamp,
	min_value bigint,
	max_value bigint,
	avg_value bigint,
	PRIMARY KEY ((event_key ,event_day),event_time)
) WITH CLUSTERING ORDER BY (event_time DESC);


CREATE TABLE metrics_agg_1min (
	event_key text,
	event_day text,
	event_time timestamp,
	min_value bigint,
	max_value bigint,
	avg_value bigint,
	PRIMARY KEY ((event_key ,event_day),event_time)
) WITH CLUSTERING ORDER BY (event_time DESC);


CREATE TABLE metrics_agg_5min (
	event_key text,
	event_day text,
	event_time timestamp,
	min_value bigint,
	max_value bigint,
	avg_value bigint,
	PRIMARY KEY ((event_key ,event_day),event_time)
) WITH CLUSTERING ORDER BY (event_time DESC);

CREATE TABLE metrics_agg_10min (
	event_key text,
	event_day text,
	event_time timestamp,
	min_value bigint,
	max_value bigint,
	avg_value bigint,
	PRIMARY KEY ((event_key ,event_day),event_time)
) WITH CLUSTERING ORDER BY (event_time DESC);
