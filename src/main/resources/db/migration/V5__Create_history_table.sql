drop table if exists t_history;
create table t_history (
    id serial primary key,
    hr_id varchar(64) not null,
    employee_id varchar(64) not null,
    employee_data jsonb not null,
    start_time timestamp,
    create_time timestamp not null default current_timestamp,
    update_time timestamp default current_timestamp,
    update_by varchar(100) default 'system'
);
create index idx_t_history_hr on t_history(hr_id);
create index idx_t_history_hr_employee on t_history(hr_id, employee_id);