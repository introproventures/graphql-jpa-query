-- Books
insert into author (id, name) values (1, 'Leo Tolstoy');
insert into book (id, title, author_id, genre) values (2, 'War and Peace', 1, 'NOVEL');
insert into book (id, title, author_id, genre) values (3, 'Anna Karenina', 1, 'NOVEL');
insert into author (id, name) values (4, 'Anton Chekhov');
insert into book (id, title, author_id, genre) values (5, 'The Cherry Orchard', 4, 'PLAY');
insert into book (id, title, author_id, genre) values (6, 'The Seagull', 4, 'PLAY');
insert into book (id, title, author_id, genre) values (7, 'Three Sisters', 4, 'PLAY');
insert into author (id, name, genre) values (8, 'Igor Dianov', 'JAVA');

insert into book_tags (book_id, tags) values (2, 'war'), (2, 'piece');
insert into book_tags (book_id, tags) values (3, 'anna'), (3, 'karenina');
insert into book_tags (book_id, tags) values (5, 'cherry'), (5, 'orchard');
insert into book_tags (book_id, tags) values (6, 'seagull');
insert into book_tags (book_id, tags) values (7, 'three'), (7, 'sisters');

insert into author_phone_numbers(phone_number, author_id) values
	('1-123-1234', 1),
	('1-123-5678', 1),
	('4-123-1234', 4),
	('4-123-5678', 4);
	