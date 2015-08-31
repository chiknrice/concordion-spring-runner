package org.chiknrice.test.spec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class Person {

    private static final Logger LOG = LoggerFactory.getLogger(Person.class);

    private String firstName;
    private String lastName;


    public Person(String firstName, String lastName) {
        LOG.info("Initializing {}", Person.class.getSimpleName());
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
