package org.chiknrice.test.spec;

import org.chiknrice.test.spec.autoscan.ScannedClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class TestSpringConcordionFixture extends BaseFixture {

    private static final Logger LOG = LoggerFactory.getLogger(TestSpringConcordionFixture.class);

    public TestSpringConcordionFixture() {
        super();
        LOG.info("Initializing {}", TestSpringConcordionFixture.class.getSimpleName());
    }

    @Autowired
    private ScannedClass scannedClass;

    public ScannedClass getScannedClass() {
        return scannedClass;
    }

}
