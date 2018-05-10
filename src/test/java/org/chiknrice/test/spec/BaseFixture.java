package org.chiknrice.test.spec;

import org.chiknrice.test.SpringifiedConcordionRunner;
import org.concordion.api.FullOGNL;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
@RunWith(SpringifiedConcordionRunner.class)
@ContextConfiguration
@FullOGNL
public abstract class BaseFixture {

    private static final Logger LOG = LoggerFactory.getLogger(BaseFixture.class);

    @Configuration
    @ComponentScan("org.chiknrice.test.spec.autoscan")
    public static class Config {
        @Bean
        public Person person() {
            return new Person("ian", "bondoc");
        }
    }

    @Autowired
    private Person person;

    public BaseFixture() {
        LOG.info("Initializing {}", BaseFixture.class.getSimpleName());
    }

    public Person getPerson() {
        return person;
    }

}
