package com.ocbc.ms.easy.care.dto;

import com.ocbc.ms.easy.care.enums.DifficultBirthTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

/**
 * 产假计算请求DTO
 */
@Data
public class MaternityLeaveRequest {

    @NotBlank(message = "工号不能为空")
    private String lanId;

    @NotBlank(message = "姓名不能为空")
    private String employeeName;

    @NotBlank(message = "城市代码不能为空")
    private String cityCode;

    @NotNull(message = "预产期不能为空")
    private LocalDate expectedDeliveryDate;

    /**
     * 医嘱天数
     */
    private Integer doctorRecommendDays;

    /**
     * 难产类型
     */
    private String difficultBirthTypeCode;

    /**
     * 是否多胞胎
     */
    // @NotNull(message = "是否为多胞胎 不能为空")
    private Boolean isMultipleBirth = false;

    /**
     * 婴儿数量
     */
    @Positive(message = "婴儿数量必须大于0")
    @NotNull(message = "婴儿数量 不能为空")
    private Integer numberOfBabies = 1;

    /**
     * 是否有 4. 晚育假｜生育假｜奖励假
     */
    @NotNull(message = "是否有4. 晚育假｜生育假｜奖励假 不能为空")
    private Boolean hasExtendedDays = false;

    /**
     * 是否难产
     */
    @NotNull(message = "是否难产 不能为空")
    private Boolean isDifficultBirth = false;

    /**
     * 难产假期天数，optional ： 广州 15/30天
     */
    private Integer additionalDystociaDays = 0;

    /**
     *  成都：是否母乳喂养，母乳多一个月
     */
    private Boolean  isBreastFeeding = false;

    /**
     * // 孩子个数
     */
    private Integer numberOfKids;

    /**
     * 是否流产
     */
    @NotNull(message = "是否流产 不能为空。如流产需传入 miscarriageLeaveDetail: {cityCode, code, days}")
    private Boolean isMiscarriage;

    /**
     * only for 绍兴
     * 是否生育一孩
     */
    private Boolean isFirstTimeBirth;

    /**
     * 流产假细节 (cityCode + index + days，其中 days的值可由前端修改【大部分在 MS enum中定好，无法override】)
     */
    // @NotNull(message = "流产假细节 不能为空")
    private MiscarriageLeaveDetail miscarriageLeaveDetail;

    /**
     *  难产类型
     * @see DifficultBirthTypeEnum
     */
    private String difficultType;

}
