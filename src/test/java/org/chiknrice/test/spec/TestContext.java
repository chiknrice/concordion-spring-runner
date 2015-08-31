package org.chiknrice.test.spec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
@Configuration
@ComponentScan("org.chiknrice.test.spec.autoscan")
public class TestContext {

    @Bean
    public Person person() {
        return new Person("ian", "bondoc");
    }

}
