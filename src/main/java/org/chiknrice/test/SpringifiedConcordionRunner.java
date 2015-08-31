package org.chiknrice.test;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.ProfileValueUtils;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.statements.RunAfterTestClassCallbacks;
import org.springframework.test.context.junit4.statements.RunBeforeTestClassCallbacks;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * A junit runner which runs concordion tests in the context of a spring container.  Code were taken from spring-test
 * and concordion.  Specifically code in this class was taken from SpringJUnit4ClassRunner
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class SpringifiedConcordionRunner extends ConcordionRunner {

    private static final Logger logger = LoggerFactory.getLogger(SpringifiedConcordionRunner.class);

    private static final Method withRulesMethod;

    static {
        withRulesMethod = ReflectionUtils.findMethod(SpringifiedConcordionRunner.class, "withRules", FrameworkMethod.class,
                Object.class, Statement.class);
        if (withRulesMethod == null) {
            throw new IllegalStateException(
                    "Failed to find withRules() method: SpringJUnit4ClassRunner requires JUnit 4.9 or higher.");
        }
        ReflectionUtils.makeAccessible(withRulesMethod);
    }

    private final TestContextManager testContextManager;

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @param clazz
     * @throws InitializationError if the test class is malformed.
     */
    public SpringifiedConcordionRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        this.testContextManager = createTestContextManager(clazz);
    }

    /**
     * Creates a new {@link TestContextManager} for the supplied test class. <p>Can be overridden by subclasses.
     *
     * @param clazz the test class to be managed
     */
    protected TestContextManager createTestContextManager(Class<?> clazz) {
        return new TestContextManager(clazz);
    }

    /**
     * Get the {@link TestContextManager} associated with this runner.
     */
    protected final TestContextManager getTestContextManager() {
        return this.testContextManager;
    }

    /**
     * Returns a description suitable for an ignored test class if the test is disabled via {@code @IfProfileValue} at
     * the class-level, and otherwise delegates to the parent implementation.
     *
     * @see ProfileValueUtils#isTestEnabledInThisEnvironment(Class)
     */
    @Override
    public Description getDescription() {
        if (!ProfileValueUtils.isTestEnabledInThisEnvironment(getTestClass().getJavaClass())) {
            return Description.createSuiteDescription(getTestClass().getJavaClass());
        }
        return super.getDescription();
    }

    /**
     * Check whether the test is enabled in the first place. This prevents classes with a non-matching {@code
     *
     * @IfProfileValue} annotation from running altogether, even skipping the execution of {@code prepareTestInstance()}
     * {@code TestExecutionListener} methods.
     * @see ProfileValueUtils#isTestEnabledInThisEnvironment(Class)
     * @see org.springframework.test.annotation.IfProfileValue
     * @see org.springframework.test.context.TestExecutionListener
     */
    @Override
    public void run(RunNotifier notifier) {
        if (!ProfileValueUtils.isTestEnabledInThisEnvironment(getTestClass().getJavaClass())) {
            notifier.fireTestIgnored(getDescription());
            return;
        }
        super.run(notifier);
    }

    /**
     * Wraps the {@link Statement} returned by the parent implementation with a {@link RunBeforeTestClassCallbacks}
     * statement, thus preserving the default functionality but adding support for the Spring TestContext Framework.
     *
     * @see RunBeforeTestClassCallbacks
     */
    @Override
    protected Statement withBeforeClasses(Statement statement) {
        Statement junitBeforeClasses = super.withBeforeClasses(statement);
        return new RunBeforeTestClassCallbacks(junitBeforeClasses, getTestContextManager());
    }

    /**
     * Wraps the {@link Statement} returned by the parent implementation with a {@link RunAfterTestClassCallbacks}
     * statement, thus preserving the default functionality but adding support for the Spring TestContext Framework.
     *
     * @see RunAfterTestClassCallbacks
     */
    @Override
    protected Statement withAfterClasses(Statement statement) {
        Statement junitAfterClasses = super.withAfterClasses(statement);
        return new RunAfterTestClassCallbacks(junitAfterClasses, getTestContextManager());
    }

    /**
     * Delegates to the parent implementation for creating the test instance and then allows the {@link
     * #getTestContextManager() TestContextManager} to prepare the test instance before returning it.
     *
     * @see TestContextManager#prepareTestInstance(Object)
     */
    @Override
    protected Object createTest() throws Exception {
        Object testInstance = super.createTest();
        getTestContextManager().prepareTestInstance(testInstance);
        return testInstance;
    }

}
