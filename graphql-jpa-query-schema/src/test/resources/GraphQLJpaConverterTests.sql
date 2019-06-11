-- Json entity
insert into json_entity (id, first_name, last_name, attributes) values
	(1, 'john', 'doe', '{"attr":{"key":["1","2","3","4","5"]}}'),
	(2, 'joe', 'smith', '{"attr":["1","2","3","4","5"]}');

insert into TASK (id, assignee, created_date, description, due_date, last_modified, last_modified_from, last_modified_to, name, priority, process_definition_id, process_instance_id, status, owner, claimed_date) values
  ('1', 'assignee', CURRENT_TIMESTAMP, 'description', null, null, null, null, 'task1', 5, 'process_definition_id', 0, 'COMPLETED'  , 'owner', null),
  ('2', 'assignee', CURRENT_TIMESTAMP, 'description', null, null, null, null, 'task2', 10, 'process_definition_id', 0, 'CREATED'  , 'owner', null),
  ('3', 'assignee', CURRENT_TIMESTAMP, 'description', null, null, null, null, 'task3', 5, 'process_definition_id', 0, 'CREATED'  , 'owner', null),
  ('4', 'assignee', CURRENT_TIMESTAMP, 'description', null, null, null, null, 'task4', 10, 'process_definition_id', 1, 'CREATED'  , 'owner', null),
  ('5', 'assignee', CURRENT_TIMESTAMP, 'description', null, null, null, null, 'task5', 5, 'process_definition_id', 1, 'COMPLETED'  , 'owner', null);

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