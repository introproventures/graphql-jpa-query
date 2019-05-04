-- Json entity
insert into json_entity (id, first_name, last_name, attributes) values
	(1, 'john', 'doe', '{"attr":{"key":["1","2","3","4","5"]}}'),
	(2, 'joe', 'smith', '{"attr":["1","2","3","4","5"]}');

insert into PROCESS_VARIABLE (create_time, execution_id, last_updated_time, name, process_instance_id, type, value) values
  (CURRENT_TIMESTAMP, 'execution_id', CURRENT_TIMESTAMP, 'document', 1, 'json', '{"value":{"key":["1","2","3","4","5"]}}');

insert into TASK_VARIABLE (create_time, execution_id, last_updated_time, name, process_instance_id, task_id, type, value) values
  (CURRENT_TIMESTAMP, 'execution_id', CURRENT_TIMESTAMP, 'variable1', 0, '1', 'string', '{"value":"data"}'),
  (CURRENT_TIMESTAMP, 'execution_id', CURRENT_TIMESTAMP, 'variable2', 0, '1', 'boolean', '{"value":true}'),
  (CURRENT_TIMESTAMP, 'execution_id', CURRENT_TIMESTAMP, 'variable3', 0, '2', 'null', '{"value":null}'),
  (CURRENT_TIMESTAMP, 'execution_id', CURRENT_TIMESTAMP, 'variable4', 0, '2', 'json', '{"value":{"key":"data"}}'),
  (CURRENT_TIMESTAMP, 'execution_id', CURRENT_TIMESTAMP, 'variable5', 1, '4', 'double', '{"value":1.2345}'),
  (CURRENT_TIMESTAMP, 'execution_id', CURRENT_TIMESTAMP, 'variable6', 1, '4', 'int', '{"value":12345}'),
  (CURRENT_TIMESTAMP, 'execution_id', CURRENT_TIMESTAMP, 'variable7', 1, '4', 'json', '{"value":[1,2,3,4,5]}');	