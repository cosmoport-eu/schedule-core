--
-- Initial core data
--
-- @encoding UTF-8
--

INSERT INTO TIMETABLE
(departure_time, type, duration, destinition, cost, status, gate_no, passangers_max, bought) VALUES
  (540, 'МАСТЕР-КЛАСС/ЭКЗОБИОЛОГИЯ', 30, 'ЛУНА', 20, 'inactive', 1, 10, 5),
  (570, 'ЭКСКУРСИЯ/ЗАПУСК СТАНЦИИ', 80, 'ВОКРУГ ЗЕМЛИ', 30, 'inactive', 1, 20, 10),
  (600, 'МИССИЯ/НА КРАЙ ВСЕЛЕННОЙ', 45, 'МАРС', 45, 'boarding', 2, 20, 10),
  (660, 'МАСТЕР-КЛАСС/ЭКЗОБИОЛОГИЯ', 120, 'ЮПИТЕР', 12, 'pending', 2, 10, 10),
  (750, 'МИССИЯ/ЗАПУСК СТАНЦИИ', 90, 'ЛУНА', 45, 'pending', 3, 10, 2),
  (840, 'МИССИЯ ЗАПУСК СТАНЦИИ', 30, 'ВОКРУГ ЗЕМЛИ', 67, 'canceled', 3, 10, 2),
  (900, 'МАСТЕР-КЛАСС/ЭКЗОБИОЛОГИЯ', 80, 'МАРС', 23, 'preorder', 4, 100, 22),
  (1005, 'ЭКСКУРСИЯ/ЗАПУСК СТАНЦИИ', 45, 'ЮПИТЕР', 45, 'pending', 4, 200, 10),
  (1020, 'МИССИЯ/ЗАПУСК СТАНЦИИ', 120, 'ЛУНА', 68, 'pending', 5, 3, 1),
  (1080, 'ЭКСКУРСИЯ/ЗАПУСК СТАНЦИИ', 90, 'ВОКРУГ ЗЕМЛИ', 34, 'pending', 6, 30, 20);