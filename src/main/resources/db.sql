create table slot_record(

   id int unsigned auto_increment,
   mcid varchar(16),
   uuid varchar(36),
   slot_name varchar(64),
   slot_file varchar(64),
   win_name varchar(64),
   win_level int default -1,
   inmoney double,
   in_item varchar(128) null,
   outmoney double default 0,
   table_name varchar(64),
   table_count int default -1,
   date DATETIME,

   primary key(id)

);