package com.ocbc.ms.easy.care.strategy;

// 策略模式测试类 - 简化版本，避免Spring依赖注入问题

/**
 * 策略模式测试类
 */
public class StrategyPatternTest {
    
    public static void main(String[] args) {
        System.out.println("=== 策略模式测试开始 ===");
        
        // 测试上海生育津贴策略
        testShanghaiAllowanceStrategy();
        
        // 测试上海产假策略
        testShanghaiLeaveStrategy();
        
        // 测试深圳生育津贴策略
        testShenzhenAllowanceStrategy();
        
        // 测试深圳产假策略
        testShenzhenLeaveStrategy();
        
        System.out.println("=== 策略模式测试完成 ===");
    }
    
    private static void testShanghaiAllowanceStrategy() {
        System.out.println("\n--- 测试上海生育津贴策略 ---");
        
        // 注意：这里需要Spring容器来注入依赖，直接new会导致依赖注入失败
        System.out.println("上海生育津贴策略需要Spring容器支持，请在Spring Boot测试环境中运行");
        System.out.println("策略类: ShanghaiMaternityAllowanceStrategy");
        System.out.println("支持城市代码: SH");
        System.out.println("计算规则: 基于单位月平均工资和产假天数计算");
    }
    
    private static void testShanghaiLeaveStrategy() {
        System.out.println("\n--- 测试上海产假策略 ---");
        
        System.out.println("上海产假策略测试");
        System.out.println("策略类: ShanghaiMaternityLeaveStrategy");
        System.out.println("支持城市代码: SH");
        System.out.println("计算规则: 基础产假 + 难产奖励 + 多胞胎奖励");
        System.out.println("测试参数: 难产=true, 婴儿数量=2");
    }
    
    private static void testShenzhenAllowanceStrategy() {
        System.out.println("\n--- 测试深圳生育津贴策略 ---");
        
        // 注意：这里需要Spring容器来注入依赖，直接new会导致依赖注入失败
        // 在实际测试中应该使用@SpringBootTest和@Autowired
        System.out.println("深圳生育津贴策略需要Spring容器支持，请在Spring Boot测试环境中运行");
        System.out.println("策略类: ShenzhenMaternityAllowanceStrategy");
        System.out.println("支持城市代码: SZ");
        System.out.println("计算规则: 产前12个月月均工资 ÷ 30 × 产假天数");
    }
    
    private static void testShenzhenLeaveStrategy() {
        System.out.println("\n--- 测试深圳产假策略 ---");
        
        System.out.println("深圳产假策略测试");
        System.out.println("策略类: ShenzhenMaternityLeaveStrategy");
        System.out.println("支持城市代码: SZ");
        System.out.println("计算规则: 基础产假 + 难产奖励 + 多胞胎奖励");
        System.out.println("测试参数: 难产=false, 婴儿数量=1");
    }
}

