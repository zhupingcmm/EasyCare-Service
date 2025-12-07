package com.hr.maternity.constant;

import com.hr.maternity.dto.MiscarriageLeaveDetail;
import com.hr.maternity.enums.CityEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 不同城市各有几个流产假的可选项规则。这里列出所有规则的明细。
 */
@Slf4j
public class MiscarriageLeaveRulesConstants {

    public static final Map<String, MiscarriageLeaveDetail> MISCARRIAGE_LEAVE_RULE_MAP;
    static {
        log.info("init MISCARRIAGE_LEAVE_RULE_MAP");
        MISCARRIAGE_LEAVE_RULE_MAP = new LinkedHashMap<>();

        //Shanghai	"1. 妊娠未满4个月流产，15天；
        //2. 妊娠满4个月流产，42天；"
        String shanghai = CityEnum.SHANGHAI.getCode();
        MISCARRIAGE_LEAVE_RULE_MAP.put(shanghai + "_1", MiscarriageLeaveDetail.builder()
                .cityCode(shanghai)
                .code("1")
                .days(15)
                .needOverrideDays(false)
                .description("1. 妊娠未满4个月流产，15天；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(shanghai + "_2", MiscarriageLeaveDetail.builder()
                .cityCode(shanghai)
                .code("2")
                .days(42)
                .needOverrideDays(false)
                .description("2. 妊娠满4个月流产，42天；")
                .build());

        //Shenzhen	"1. 怀孕未满4个月终止妊娠的，根据医疗机构的意见，计15天至30天；
        //2. 怀孕4个月以上7个月以下终止妊娠的，计42天；
        //3. 怀孕满7个月终止妊娠的，计75天；"
        String shenzhen = CityEnum.SHENZHEN.getCode();
        MISCARRIAGE_LEAVE_RULE_MAP.put(shenzhen + "_1", MiscarriageLeaveDetail.builder()
                .cityCode(shenzhen)
                .code("1")
                .days(0)
                .needOverrideDays(true)
                .description("1. 怀孕未满4个月终止妊娠的，根据医疗机构的意见，计15天至30天；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(shenzhen + "_2", MiscarriageLeaveDetail.builder()
                .cityCode(shenzhen)
                .code("2")
                .days(42)
                .needOverrideDays(false)
                .description("2. 怀孕4个月以上7个月以下终止妊娠的，计42天；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(shenzhen + "_3", MiscarriageLeaveDetail.builder()
                .cityCode(shenzhen)
                .code("3")
                .days(75)
                .needOverrideDays(false)
                .description("3. 怀孕满7个月终止妊娠的，计75天；")
                .build());

        //Guangzhou	"1.怀孕4个月以下：根据医疗机构意见，计算15-30天（诊断证明必须有明确写明休假天数）；
        //2.怀孕4个月以上（含4个月）至7个月以下：42天；
        //3.怀孕满7个月（含7个月）以上：75天；"
        String guangzhou = CityEnum.GUANGZHOU.getCode();
        MISCARRIAGE_LEAVE_RULE_MAP.put(guangzhou + "_1", MiscarriageLeaveDetail.builder()
                .cityCode(guangzhou)
                .code("1")
                .days(0)
                .needOverrideDays(true)
                .description("1.怀孕4个月以下：根据医疗机构意见，计算15-30天（诊断证明必须有明确写明休假天数）；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(guangzhou + "_2", MiscarriageLeaveDetail.builder()
                .cityCode(guangzhou)
                .code("2")
                .days(42)
                .needOverrideDays(false)
                .description("2.怀孕4个月以上（含4个月）至7个月以下：42天；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(guangzhou + "_3", MiscarriageLeaveDetail.builder()
                .cityCode(guangzhou)
                .code("3")
                .days(75)
                .needOverrideDays(false)
                .description("3.怀孕满7个月（含7个月）以上：75天；")
                .build());

        //Tianjin	"1. 妊娠未满4个月流产，15天；
        //2. 妊娠满4个月流产，42天；"
        String tianjin = CityEnum.TIANJIN.getCode();
        MISCARRIAGE_LEAVE_RULE_MAP.put(tianjin + "_1", MiscarriageLeaveDetail.builder()
                .cityCode(tianjin)
                .code("3")
                .days(15)
                .needOverrideDays(false)
                .description("1. 妊娠未满4个月流产，15天；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(tianjin + "_2", MiscarriageLeaveDetail.builder()
                .cityCode(tianjin)
                .code("2")
                .days(42)
                .needOverrideDays(false)
                .description("2. 妊娠满4个月流产，42天；")
                .build());

        //Shaoxing	"1. 怀孕不满4个月流产的，享受产假15天；
        //2. 怀孕满4个月流产的，享受产假42天；"
        String shaoxing = CityEnum.SHAOXING.getCode();
        MISCARRIAGE_LEAVE_RULE_MAP.put(shaoxing + "_1", MiscarriageLeaveDetail.builder()
                .cityCode(shaoxing)
                .code("3")
                .days(15)
                .needOverrideDays(false)
                .description("1. 怀孕不满4个月流产的，享受产假15天；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(shaoxing + "_2", MiscarriageLeaveDetail.builder()
                .cityCode(shaoxing)
                .code("2")
                .days(42)
                .needOverrideDays(false)
                .description("2. 怀孕满4个月流产的，享受产假42天；")
                .build());

        //Xiamen	"1. 流产（含人工流产或引产）：怀孕3个月（妊娠）以内流产的15天；
        //2. 怀孕3个月（妊娠）以上流产的42天；
        //3. 怀孕满7个月（妊娠）及以上流产的98天；
        //注意：孕期妊娠月以28天即4周为1个月计算。"
        String xiamen = CityEnum.XIAMEN.getCode();
        MISCARRIAGE_LEAVE_RULE_MAP.put(xiamen + "_1", MiscarriageLeaveDetail.builder()
                .cityCode(xiamen)
                .code("3")
                .days(15)
                .needOverrideDays(false)
                .description("1. 流产（含人工流产或引产）：怀孕3个月（妊娠）以内流产的15天；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(xiamen + "_2", MiscarriageLeaveDetail.builder()
                .cityCode(xiamen)
                .code("2")
                .days(42)
                .needOverrideDays(false)
                .description("2. 怀孕3个月（妊娠）以上流产的42天；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(xiamen + "_3", MiscarriageLeaveDetail.builder()
                .cityCode(xiamen)
                .code("3")
                .days(98)
                .needOverrideDays(false)
                .description("3. 怀孕满7个月（妊娠）及以上流产的98天；注意：孕期妊娠月以28天即4周为1个月计算。")
                .build());

        //Chengdu	"1. 怀孕未满4个月流产的，享受15天产假；
        //2. 怀孕满4个月流产的，享受42天产假;"
        String chengdu = CityEnum.CHENGDU.getCode();
        MISCARRIAGE_LEAVE_RULE_MAP.put(chengdu + "_1", MiscarriageLeaveDetail.builder()
                .cityCode(chengdu)
                .code("3")
                .days(15)
                .needOverrideDays(false)
                .description("1. 怀孕未满4个月流产的，享受15天产假；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(chengdu + "_2", MiscarriageLeaveDetail.builder()
                .cityCode(chengdu)
                .code("2")
                .days(42)
                .needOverrideDays(false)
                .description("2. 怀孕满4个月流产的，享受42天产假;")
                .build());

        //Suzhou	"1、怀孕不满2个月流产的，享受不少于20天的产假；
        //2、怀孕满2个月不满3个月流产的，享受不少于30天的产假；
        //3、怀孕满3个月不满7个月流产、引产的，享受不少于42天的产假；
        //4、怀孕满7个月引产的，享受不少于98天的产假；"
        String suzhou = CityEnum.SUZHOU.getCode();
        MISCARRIAGE_LEAVE_RULE_MAP.put(suzhou + "_1", MiscarriageLeaveDetail.builder()
                .cityCode(suzhou)
                .code("3")
                .days(0)
                .needOverrideDays(true)
                .description("1、怀孕不满2个月流产的，享受不少于20天的产假；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(suzhou + "_2", MiscarriageLeaveDetail.builder()
                .cityCode(suzhou)
                .code("2")
                .days(0)
                .needOverrideDays(true)
                .description("2、怀孕满2个月不满3个月流产的，享受不少于30天的产假；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(suzhou + "_3", MiscarriageLeaveDetail.builder()
                .cityCode(suzhou)
                .code("3")
                .days(0)
                .needOverrideDays(true)
                .description("3、怀孕满3个月不满7个月流产、引产的，享受不少于42天的产假；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(suzhou + "_4", MiscarriageLeaveDetail.builder()
                .cityCode(suzhou)
                .code("3")
                .days(0)
                .needOverrideDays(true)
                .description("4、怀孕满7个月引产的，享受不少于98天的产假；")
                .build());

        //Qingdao	"1. 女职工怀孕未满4个月流产的，享受15天产假；
        //2. 怀孕满4个月流产的，享受42天产假；"
        String qingdao = CityEnum.QINGDAO.getCode();
        MISCARRIAGE_LEAVE_RULE_MAP.put(qingdao + "_1", MiscarriageLeaveDetail.builder()
                .cityCode(qingdao)
                .code("3")
                .days(15)
                .needOverrideDays(false)
                .description("1. 女职工怀孕未满4个月流产的，享受15天产假；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(qingdao + "_2", MiscarriageLeaveDetail.builder()
                .cityCode(qingdao)
                .code("2")
                .days(42)
                .needOverrideDays(false)
                .description("2. 怀孕满4个月流产的，享受42天产假；")
                .build());

        //Beijing	"1. 怀孕未满4个月流产休假15天至30天（根据假条）；
        //2. 怀孕满4个月-7个月流产休假42天；
        //3. 怀孕7个月以上按正常产休假；"
        String beijing = CityEnum.BEIJING.getCode();
        MISCARRIAGE_LEAVE_RULE_MAP.put(beijing + "_1", MiscarriageLeaveDetail.builder()
                .cityCode(beijing)
                .code("3")
                .days(0)
                .needOverrideDays(true)
                .description("1. 怀孕未满4个月流产休假15天至30天（根据假条）；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(beijing + "_2", MiscarriageLeaveDetail.builder()
                .cityCode(beijing)
                .code("2")
                .days(42)
                .needOverrideDays(false)
                .description("2. 怀孕满4个月-7个月流产休假42天；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(beijing + "_3", MiscarriageLeaveDetail.builder()
                .cityCode(beijing)
                .code("3")
                .days(0)
                .needOverrideDays(true)
                .description("3. 怀孕7个月以上按正常产休假；")
                .build());

        //Chongqing	"1. 四个月以下流产 15天
        //2. 四个月以上流产 42天
        //3. 宫外孕                42天"
        String chongqing = CityEnum.CHONGQING.getCode();
        MISCARRIAGE_LEAVE_RULE_MAP.put(chongqing + "_1", MiscarriageLeaveDetail.builder()
                .cityCode(chongqing)
                .code("3")
                .days(15)
                .needOverrideDays(false)
                .description("1. 四个月以下流产 15天")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(chongqing + "_2", MiscarriageLeaveDetail.builder()
                .cityCode(chongqing)
                .code("2")
                .days(42)
                .needOverrideDays(false)
                .description("2. 四个月以上流产 42天")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(chongqing + "_3", MiscarriageLeaveDetail.builder()
                .cityCode(chongqing)
                .code("3")
                .days(42)
                .needOverrideDays(false)
                .description("3. 宫外孕       42天")
                .build());

        //Zhuhai/Foshan	"1.怀孕4个月以下：根据医疗机构意见，计算15-30天（诊断证明必须有明确写明休假天数）；
        //2.怀孕4个月以上（含4个月）至7个月以下：42天
        //3.怀孕满7个月（含7个月）以上：75天"
        String zhuhai = CityEnum.ZHUHAI.getCode();
        MISCARRIAGE_LEAVE_RULE_MAP.put(zhuhai + "_1", MiscarriageLeaveDetail.builder()
                .cityCode(zhuhai)
                .code("3")
                .days(0)
                .needOverrideDays(true)
                .description("1.怀孕4个月以下：根据医疗机构意见，计算15-30天（诊断证明必须有明确写明休假天数）；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(zhuhai + "_2", MiscarriageLeaveDetail.builder()
                .cityCode(zhuhai)
                .code("2")
                .days(42)
                .needOverrideDays(false)
                .description("2.怀孕4个月以上（含4个月）至7个月以下：42天")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(zhuhai + "_3", MiscarriageLeaveDetail.builder()
                .cityCode(zhuhai)
                .code("3")
                .days(75)
                .needOverrideDays(false)
                .description("3.怀孕满7个月（含7个月）以上：75天")
                .build());

        String foshan = CityEnum.FOSHAN.getCode();
        MISCARRIAGE_LEAVE_RULE_MAP.put(foshan + "_1", MiscarriageLeaveDetail.builder()
                .cityCode(foshan)
                .code("3")
                .days(0)
                .needOverrideDays(true)
                .description("1.怀孕4个月以下：根据医疗机构意见，计算15-30天（诊断证明必须有明确写明休假天数）；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(foshan + "_2", MiscarriageLeaveDetail.builder()
                .cityCode(foshan)
                .code("2")
                .days(42)
                .needOverrideDays(false)
                .description("2.怀孕4个月以上（含4个月）至7个月以下：42天")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(foshan + "_3", MiscarriageLeaveDetail.builder()
                .cityCode(foshan)
                .code("3")
                .days(75)
                .needOverrideDays(false)
                .description("3.怀孕满7个月（含7个月）以上：75天")
                .build());

        //Wuhan	"1. 妊娠不满12周流产的，产假为30天；
        //2. 妊娠满12周不满28周流（引）产的，产假为45天；
        //3. 妊娠满28周以上引产的，产假为98天；"
        String wuhan = CityEnum.WUHAN.getCode();
        MISCARRIAGE_LEAVE_RULE_MAP.put(wuhan + "_1", MiscarriageLeaveDetail.builder()
                .cityCode(wuhan)
                .code("3")
                .days(30)
                .needOverrideDays(false)
                .description("1. 妊娠不满12周流产的，产假为30天；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(wuhan + "_2", MiscarriageLeaveDetail.builder()
                .cityCode(wuhan)
                .code("2")
                .days(45)
                .needOverrideDays(false)
                .description("2. 妊娠满12周不满28周流（引）产的，产假为45天；")
                .build());
        MISCARRIAGE_LEAVE_RULE_MAP.put(wuhan + "_3", MiscarriageLeaveDetail.builder()
                .cityCode(wuhan)
                .code("3")
                .days(98)
                .needOverrideDays(false)
                .description("3. 妊娠满28周以上引产的，产假为98天；")
                .build());

        log.info("MISCARRIAGE_LEAVE_RULE_MAP={}", MISCARRIAGE_LEAVE_RULE_MAP);
        log.info("init MISCARRIAGE_LEAVE_RULE_MAP end");
    };

}
