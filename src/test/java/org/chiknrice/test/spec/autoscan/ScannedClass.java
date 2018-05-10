package org.chiknrice.test.spec.autoscan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
@Component
public class ScannedClass implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ScannedClass.class);

    public ScannedClass() {
        LOG.info("Initializing {}", ScannedClass.class.getSimpleName());
    }

    public String getMessage() {
        return "Hello!";
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        LOG.info("> {}", event.getClass().getSimpleName());
    }
}
