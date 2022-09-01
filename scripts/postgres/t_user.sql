/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

drop table if exists t_user;
create table t_user
(
    id          serial4                             not null,
    name        varchar(20)                         not null,
    conf        jsonb                               not null,
    setting     jsonb,
    create_time timestamp default current_timestamp not null,
    update_time timestamp default current_timestamp not null
);


drop table if exists t_student;
create table t_student
(
    id    serial4 not null,
    tags  int4[]  not null,
    names varchar[]
);