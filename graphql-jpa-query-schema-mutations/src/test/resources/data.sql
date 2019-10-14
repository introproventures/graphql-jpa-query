-- Insert Code Lists
insert into student (id, name) values
  (1, 'org.crygier.graphql.model.starwars.Gender'),
  (2, 'org.crygier.graphql.model.starwars.Gender');



insert into author (id, name, genre) values
  (1, 'Pushkin', 'NOVEL'),
  (2, 'Lermontov', 'PLAY'),
  (3, 'Tolstoy', 'PLAY');


insert into book (id, title, author_id, genre) values
  (1, 'book1', 1, 'NOVEL'),
  (2, 'book2', 1, 'PLAY'),
  (3, 'book3', 1, 'PLAY'),
  (4, 'book4', 3, 'PLAY');


insert into publishing_house (id, name) values
  (1, 'house 1'),
  (2, 'house 2');

