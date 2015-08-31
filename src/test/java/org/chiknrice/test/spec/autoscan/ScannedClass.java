package org.chiknrice.test.spec.autoscan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
@Component
public class ScannedClass {

    private static final Logger LOG = LoggerFactory.getLogger(ScannedClass.class);

    public ScannedClass() {
        LOG.info("Initializing {}", ScannedClass.class.getSimpleName());
    }

    public String getMessage() {
        return "Hello!";
    }

}
