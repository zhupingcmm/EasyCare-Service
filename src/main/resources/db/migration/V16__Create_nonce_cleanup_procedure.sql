-- 创建 nonce 清理存储过程
CREATE OR REPLACE FUNCTION delete_yesterday_nonces(threshold integer)
RETURNS character varying
SECURITY INVOKER
AS $FUNCTIONS$
declare
    loopCount int;
    totalRecords int;
    delCnt int;
begin
    totalRecords := (select count(*) FROM nonce WHERE expires_at < current_date and expires_at >= current_date-1);
    raise notice 'Total Records for yesterday : % ', totalRecords;
    loopCount = ceil(totalRecords/threshold);
    raise notice 'Total loop count : % ', loopCount;
    
    for i in 1..loopCount loop
        raise notice 'Loop : % ', i;
        delete from nonce
        where id in (select id from nonce 
                     where expires_at >= current_date-1 and expires_at < current_date 
                     limit threshold);
        get diagnostics delCnt = ROW_COUNT;
        raise notice 'Total number of deleted records : % ', delCnt;
    end loop;
    
    return 'SUCCESS';
exception
    when others then
        raise exception 'Error: Failed to delete records';
end
$FUNCTIONS$
LANGUAGE plpgsql;


