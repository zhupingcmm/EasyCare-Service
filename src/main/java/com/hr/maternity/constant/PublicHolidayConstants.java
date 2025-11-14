package com.hr.maternity.constant;

import java.time.LocalDate;
import java.util.Set;

/**
 * 需要顺延的公共节假日
 */
public class PublicHolidayConstants {
    public static final Set<LocalDate> HOLIDAY = Set.of(
LocalDate.of(2023,  1,  1), // "元旦"
LocalDate.of(2023,  1, 22), // "春节"
LocalDate.of(2023,  1, 23), // "春节"
LocalDate.of(2023,  1, 24), // "春节"
LocalDate.of(2023,  4,  5), // "清明节"
LocalDate.of(2023,  4, 29), // "劳动节"
LocalDate.of(2023,  6, 22), // "端午节"
LocalDate.of(2023,  9, 29), // "中秋节和国庆节"
LocalDate.of(2023, 10,  1), // "中秋节和国庆节"
LocalDate.of(2023, 10,  2), // "中秋节和国庆节"
LocalDate.of(2023, 10,  3), // "中秋节和国庆节"

LocalDate.of(2024,  1,  1), // "元旦"
LocalDate.of(2024,  2, 10), // "春节"
LocalDate.of(2024,  2, 11), // "春节"
LocalDate.of(2024,  2, 12), // "春节"
LocalDate.of(2024,  4,  4), // "清明节"
LocalDate.of(2024,  5,  1), // "劳动节"
LocalDate.of(2024,  6, 10), // "端午节"
LocalDate.of(2024,  9, 17), // "中秋节"
LocalDate.of(2024, 10,  1), // "国庆节"
LocalDate.of(2024, 10,  2), // "国庆节"
LocalDate.of(2024, 10,  3), // "国庆节"

LocalDate.of(2025,  1,  1), // "元旦"
LocalDate.of(2025,  1, 28), // "春节"
LocalDate.of(2025,  1, 29), // "春节"
LocalDate.of(2025,  1, 30), // "春节"
LocalDate.of(2025,  1, 31), // "春节"
LocalDate.of(2025,  4,  4), // "清明节"
LocalDate.of(2025,  5,  1), // "劳动节"
LocalDate.of(2025,  5,  2), // "劳动节"w
LocalDate.of(2025,  5, 31), // "端午节"
LocalDate.of(2025, 10,  1), // "国庆节、中秋节"
LocalDate.of(2025, 10,  2), // "国庆节、中秋节"
LocalDate.of(2025, 10,  3), // "国庆节、中秋节"
LocalDate.of(2025, 10,  6)  // "国庆节、中秋节"
    );
}
