package indi.melon.branch;

import indi.melon.branch.chooser.configuration.BranchChooserContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Properties;

/**
 * @author wangmenglong
 * @since 2024/8/26 16:29
 */
@SpringBootApplication
@ComponentScan({"indi.melon.branch.chooser", "indi.melon.branch"})
public class TestConfiguration {
    public static void main(String[] args) {
        SpringApplication.run(TestConfiguration.class, args);
    }

    @Bean
    public BranchChooserContext branchChooserContext() {
        Properties properties = new Properties();
        properties.setProperty("env", "beijing");
        return () -> properties;
    }
}
