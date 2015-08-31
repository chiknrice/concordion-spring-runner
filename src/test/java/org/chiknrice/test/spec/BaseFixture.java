package org.chiknrice.test.spec;

import org.chiknrice.test.SpringifiedConcordionRunner;
import org.concordion.api.FullOGNL;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
@RunWith(SpringifiedConcordionRunner.class)
@ContextConfiguration(classes = TestContext.class)
@FullOGNL
public abstract class BaseFixture {

    private static final Logger LOG = LoggerFactory.getLogger(BaseFixture.class);

    @Autowired
    private Person person;

    public BaseFixture() {
        LOG.info("Initializing {}", BaseFixture.class.getSimpleName());
    }

    public Person getPerson() {
        return person;
    }

}
