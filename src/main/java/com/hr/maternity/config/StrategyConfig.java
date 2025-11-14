package com.hr.maternity.config;

import com.hr.maternity.strategy.MaternityAllowanceStrategy;
import com.hr.maternity.strategy.MaternityLeaveStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 策略装配配置：
 * 将所有策略实现类按城市代码聚合成Map，供Service层按cityCode检索。
 */
@Configuration
public class StrategyConfig {

    @Bean
    public Map<String, MaternityLeaveStrategy> maternityLeaveStrategyMap(List<MaternityLeaveStrategy> strategies) {
        Map<String, MaternityLeaveStrategy> map = new HashMap<>();
        for (MaternityLeaveStrategy s : strategies) {
            map.put(s.getSupportedCityCode(), s);
        }
        return map;
    }

    @Bean
    public Map<String, MaternityAllowanceStrategy> maternityAllowanceStrategyMap(List<MaternityAllowanceStrategy> strategies) {
        Map<String, MaternityAllowanceStrategy> map = new HashMap<>();
        for (MaternityAllowanceStrategy s : strategies) {
            map.put(s.getSupportedCityCode(), s);
        }
        return map;
    }
}
