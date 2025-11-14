-- Alter remark column to jsonb (existing data is NULL)
ALTER TABLE city
    ALTER COLUMN remark TYPE jsonb
    USING remark::jsonb;

-- Populate remark jsonb: allowanceToIndividual and dystociaType
-- allowanceToIndividual = true for codes: TJ(天津), SX(绍兴), XM(厦门), SH(上海), QD(青岛); else false
-- dystociaType (for GZ only) = [
--   {code:'SEVERE_DYSTOCIA', desc:'难产（剖腹产、会阴Ⅲ度破裂）'},
--   {code:'ASSISTED_DELIVERY', desc:'吸引产、钳产、臀位牵引产'}
-- ]; others NULL
UPDATE city
SET remark = jsonb_build_object(
    'allowanceToIndividual', CASE
        WHEN code IN ('TJ','SX','XM','SH','QD') THEN TRUE
        ELSE FALSE
    END,
    'dystociaType', CASE
        WHEN code = 'GZ' THEN jsonb_build_array(
            jsonb_build_object('code','SEVERE_DYSTOCIA','desc','难产（剖腹产、会阴Ⅲ度破裂）'),
            jsonb_build_object('code','ASSISTED_DELIVERY','desc','吸引产、钳产、臀位牵引产')
        )
        ELSE NULL
    END
);
