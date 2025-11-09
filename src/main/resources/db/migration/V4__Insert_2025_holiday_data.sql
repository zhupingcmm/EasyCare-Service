-- 插入2025年中国节假日数据
INSERT INTO holiday (year, region, date, name, name_cn, name_en, type) VALUES
-- 元旦
(2025, 'CN', '2025-01-01', '元旦', '元旦', 'New Year''s Day', 'public_holiday'),

-- 春节及相关调休
(2025, 'CN', '2025-01-26', '春节补班', '春节补班', 'Spring Festival Workday', 'transfer_workday'),
(2025, 'CN', '2025-01-28', '春节', '春节', 'Chinese New Year', 'public_holiday'),
(2025, 'CN', '2025-01-29', '春节', '春节', 'Chinese New Year', 'public_holiday'),
(2025, 'CN', '2025-01-30', '春节', '春节', 'Chinese New Year', 'public_holiday'),
(2025, 'CN', '2025-01-31', '春节', '春节', 'Chinese New Year', 'public_holiday'),
(2025, 'CN', '2025-02-01', '春节', '春节', 'Chinese New Year', 'public_holiday'),
(2025, 'CN', '2025-02-02', '春节', '春节', 'Chinese New Year', 'public_holiday'),
(2025, 'CN', '2025-02-03', '春节', '春节', 'Chinese New Year', 'public_holiday'),
(2025, 'CN', '2025-02-04', '春节', '春节', 'Chinese New Year', 'public_holiday'),
(2025, 'CN', '2025-02-08', '春节补班', '春节补班', 'Spring Festival Workday', 'transfer_workday'),

-- 清明节
(2025, 'CN', '2025-04-04', '清明节', '清明节', 'Tomb-Sweeping Day', 'public_holiday'),
(2025, 'CN', '2025-04-05', '清明节', '清明节', 'Tomb-Sweeping Day', 'public_holiday'),
(2025, 'CN', '2025-04-06', '清明节', '清明节', 'Tomb-Sweeping Day', 'public_holiday'),

-- 劳动节及相关调休
(2025, 'CN', '2025-04-27', '劳动节补班', '劳动节补班', 'Labor Day Workday', 'transfer_workday'),
(2025, 'CN', '2025-05-01', '劳动节', '劳动节', 'Labor Day', 'public_holiday'),
(2025, 'CN', '2025-05-02', '劳动节', '劳动节', 'Labor Day', 'public_holiday'),
(2025, 'CN', '2025-05-03', '劳动节', '劳动节', 'Labor Day', 'public_holiday'),
(2025, 'CN', '2025-05-04', '劳动节', '劳动节', 'Labor Day', 'public_holiday'),
(2025, 'CN', '2025-05-05', '劳动节', '劳动节', 'Labor Day', 'public_holiday'),

-- 端午节
(2025, 'CN', '2025-05-31', '端午节', '端午节', 'Dragon Boat Festival', 'public_holiday'),
(2025, 'CN', '2025-06-01', '端午节', '端午节', 'Dragon Boat Festival', 'public_holiday'),
(2025, 'CN', '2025-06-02', '端午节', '端午节', 'Dragon Boat Festival', 'public_holiday'),

-- 国庆节、中秋节及相关调休
(2025, 'CN', '2025-09-28', '国庆节补班', '国庆节补班', 'National Day Workday', 'transfer_workday'),
(2025, 'CN', '2025-10-01', '国庆节、中秋节', '国庆节、中秋节', 'National Day & Mid-Autumn Festival', 'public_holiday'),
(2025, 'CN', '2025-10-02', '国庆节、中秋节', '国庆节、中秋节', 'National Day & Mid-Autumn Festival', 'public_holiday'),
(2025, 'CN', '2025-10-03', '国庆节、中秋节', '国庆节、中秋节', 'National Day & Mid-Autumn Festival', 'public_holiday'),
(2025, 'CN', '2025-10-04', '国庆节、中秋节', '国庆节、中秋节', 'National Day & Mid-Autumn Festival', 'public_holiday'),
(2025, 'CN', '2025-10-05', '国庆节、中秋节', '国庆节、中秋节', 'National Day & Mid-Autumn Festival', 'public_holiday'),
(2025, 'CN', '2025-10-06', '国庆节、中秋节', '国庆节、中秋节', 'National Day & Mid-Autumn Festival', 'public_holiday'),
(2025, 'CN', '2025-10-07', '国庆节、中秋节', '国庆节、中秋节', 'National Day & Mid-Autumn Festival', 'public_holiday'),
(2025, 'CN', '2025-10-08', '国庆节、中秋节', '国庆节、中秋节', 'National Day & Mid-Autumn Festival', 'public_holiday'),
(2025, 'CN', '2025-10-11', '国庆节补班', '国庆节补班', 'National Day Workday', 'transfer_workday');

-- 验证插入的数据
-- SELECT year, region, COUNT(*) as total_records,
--        COUNT(CASE WHEN type = 'public_holiday' THEN 1 END) as holidays,
--        COUNT(CASE WHEN type = 'transfer_workday' THEN 1 END) as workdays
-- FROM holiday 
-- WHERE year = 2025 AND region = 'CN'
-- GROUP BY year, region;
