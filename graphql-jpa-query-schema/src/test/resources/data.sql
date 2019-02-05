-- Insert Code Lists
insert into code_list (id, type, code, description, sequence, active, parent_id) values 
  (0, 'org.crygier.graphql.model.starwars.Gender', 'Male', 'Male', 1, true, null),
  (1, 'org.crygier.graphql.model.starwars.Gender', 'Female', 'Female', 2, true, null);

-- Insert Droid Functions
insert into droid_function(id, function) values
( '1000', 'Protocol'),
( '1001', 'Astromech');

-- Insert Droids
insert into character (id, name, primary_function, dtype) values
    ('2000', 'C-3PO', '1000', 'Droid'),
    ('2001', 'R2-D2', '1001', 'Droid');

-- Insert Humans
insert into character (id, name, home_planet, favorite_droid_id, dtype, gender_code_id) values
    ('1000', 'Luke Skywalker', 'Tatooine', '2000', 'Human', 0),
    ('1001', 'Darth Vader', 'Tatooine', '2001', 'Human', 0),
    ('1002', 'Han Solo', NULL, NULL, 'Human', 0),
    ('1003', 'Leia Organa', 'Alderaan', NULL, 'Human', 1),
    ('1004', 'Wilhuff Tarkin', NULL, NULL, 'Human', 0);

-- Luke's friends
insert into character_friends (source_id, friend_id) values
    ('1000', '1002'),
    ('1000', '1003'),
    ('1000', '2000'),
    ('1000', '2001');

-- Luke Appears in
insert into character_appears_in (character_id, appears_in) values
    ('1000', 3),
    ('1000', 4),
    ('1000', 5),
    ('1000', 6);

-- Vader's friends
insert into character_friends (source_id, friend_id) values
    ('1001', '1004');

-- Vader Appears in
insert into character_appears_in (character_id, appears_in) values
    ('1001', 3),
    ('1001', 4),
    ('1001', 5);

-- Solo's friends
insert into character_friends (source_id, friend_id) values
    ('1002', '1000'),
    ('1002', '1003'),
    ('1002', '2001');

-- Solo Appears in
insert into character_appears_in (character_id, appears_in) values
    ('1002', 3),
    ('1002', 4),
    ('1002', 5),
    ('1002', 6);

-- Leia's friends
insert into character_friends (source_id, friend_id) values
    ('1003', '1000'),
    ('1003', '1002'),
    ('1003', '2000'),
    ('1003', '2001');

-- Leia Appears in
insert into character_appears_in (character_id, appears_in) values
    ('1003', 3),
    ('1003', 4),
    ('1003', 5),
    ('1003', 6);

-- Wilhuff's friends
insert into character_friends (source_id, friend_id) values
    ('1004', '1001');

-- Wilhuff Appears in
insert into character_appears_in (character_id, appears_in) values
    ('1004', 3);

-- C3PO's friends
insert into character_friends (source_id, friend_id) values
    ('2000', '1000'),
    ('2000', '1002'),
    ('2000', '1003'),
    ('2000', '2001');

-- C3PO Appears in
insert into character_appears_in (character_id, appears_in) values
    ('2000', 3),
    ('2000', 4),
    ('2000', 5),
    ('2000', 6);

-- R2's friends
insert into character_friends (source_id, friend_id) values
    ('2001', '1000'),
    ('2001', '1002'),
    ('2001', '1003');

-- R2 Appears in
insert into character_appears_in (character_id, appears_in) values
    ('2001', 3),
    ('2001', 4),
    ('2001', 5),
    ('2001', 6);

-- Things
insert into thing (id, type) values
    ('2D1EBC5B7D2741979CF0E84451C5BBB1', 'Thing1');
    
-- Books
insert into author (id, name, genre) values (1, 'Leo Tolstoy', 'NOVEL');
insert into book (id, title, author_id, genre, publication_date, description)
values (2, 'War and Peace', 1, 'NOVEL', '1869-01-01', 'The novel chronicles the history of the French invasion of Russia and the impact of the Napoleonic era on Tsarist society through the stories of five Russian aristocratic families.');
insert into book (id, title, author_id, genre, publication_date, description)
values (3, 'Anna Karenina', 1, 'NOVEL', '1877-04-01', 'A complex novel in eight parts, with more than a dozen major characters, it is spread over more than 800 pages (depending on the translation), typically contained in two volumes.');
insert into author (id, name, genre) values (4, 'Anton Chekhov', 'PLAY');
insert into book (id, title, author_id, genre, publication_date, description)
values (5, 'The Cherry Orchard', 4, 'PLAY', '1904-01-17', 'The play concerns an aristocratic Russian landowner who returns to her family estate (which includes a large and well-known cherry orchard) just before it is auctioned to pay the mortgage.');
insert into book (id, title, author_id, genre, publication_date, description)
values (6, 'The Seagull', 4, 'PLAY', '1896-10-17', 'It dramatises the romantic and artistic conflicts between four characters');
insert into book (id, title, author_id, genre, publication_date, description)
values (7, 'Three Sisters', 4, 'PLAY', '1900-01-01', 'The play is sometimes included on the short list of Chekhov''s outstanding plays, along with The Cherry Orchard, The Seagull and Uncle Vanya.[1]');

insert into author_phone_numbers(phone_number, author_id) values
	('1-123-1234', 1),
	('1-123-5678', 1),
	('4-123-1234', 4),
	('4-123-5678', 4);
	
-- Car
insert into Car (id, brand, identification) values
	(1, 'Ford', 'xxxxx'),
	(2, 'Cadillac', 'yyyyy'),
	(3, 'Toyota', 'zzzzz');

	
-- Boat
insert into Boat (id, country, identification) values
	(1, 'EN', '12345'),
	(2, 'EN', '23456'),
	(1, 'FR', '34567');

-- Calculate entity
insert into calculated_entity (id, title, info) values
	(1, 'title 1', 'inf 1'),
	(2, 'title 2', 'inf 2');
