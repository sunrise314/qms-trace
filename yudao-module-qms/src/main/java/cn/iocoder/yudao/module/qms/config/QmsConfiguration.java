package cn.iocoder.yudao.module.qms.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * QMS 模块自动配置。
 *
 * yudao 父工程的 @SpringBootApplication 会扫描 cn.iocoder.yudao.module.qms 包，
 * 所以本类的主要作用是：
 * 1. 确保 qms 包被 Spring 扫描到（组件注册）
 * 2. 明确 @MapperScan 指定 MyBatis Mapper 扫描路径
 *
 * AQL 抽样方案表、SPC 控制图系数表均内置在 framework 层（纯 Java 常量），
 * 无需在此注册额外 Bean；商用扩展点（如把 AQL 主表配置化到数据库）可在此引入。
 */
@SpringBootApplication(scanBasePackages = "cn.iocoder.yudao.module.qms")
@MapperScan("cn.iocoder.yudao.module.qms.dal.mysql")
public class QmsConfiguration {
}
