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

insert into book_publishers(book_id, name, country) values
	(3, 'Independent', 'UK'), (3, 'Amazon', 'US'),
	(2, 'Willey', 'US'), (2, 'Simon', 'EU');
