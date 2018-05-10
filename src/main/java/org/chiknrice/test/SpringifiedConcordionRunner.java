package org.chiknrice.test;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.Ignore;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.ProfileValueUtils;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.context.junit4.statements.*;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * An extension of {@code ConcordionRunner} which runs concordion tests in the context of a spring container.  Most of
 * the code were taken from SpringJUnit4ClassRunner which concerns initializing fixture instances using {@code
 * TestContextManager#prepareTestInstance} and hooking the {@code TestContextManager} to JUnit's test phases.
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class SpringifiedConcordionRunner extends ConcordionRunner {

    private static final Logger logger = LoggerFactory.getLogger(SpringifiedConcordionRunner.class);

    private static final Method withRulesMethod;

    static {
        Assert.state(ClassUtils.isPresent("org.junit.internal.Throwables", SpringifiedConcordionRunner.class.getClassLoader()),
                "SpringifiedConcordionRunner requires JUnit 4.12 or higher.");

        Method method = ReflectionUtils.findMethod(SpringifiedConcordionRunner.class, "withRules",
                FrameworkMethod.class, Object.class, Statement.class);
        Assert.state(method != null, "SpringifiedConcordionRunner requires JUnit 4.12 or higher");
        ReflectionUtils.makeAccessible(method);
        withRulesMethod = method;
    }

    private final TestContextManager testContextManager;

    private static void ensureSpringRulesAreNotPresent(Class<?> testClass) {
        for (Field field : testClass.getFields()) {
            Assert.state(!SpringClassRule.class.isAssignableFrom(field.getType()), () -> String.format(
                    "Detected SpringClassRule field in test class [%s], " +
                            "but SpringClassRule cannot be used with the SpringifiedConcordionRunner.", testClass.getName()));
            Assert.state(!SpringMethodRule.class.isAssignableFrom(field.getType()), () -> String.format(
                    "Detected SpringMethodRule field in test class [%s], " +
                            "but SpringMethodRule cannot be used with the SpringifiedConcordionRunner.", testClass.getName()));
        }
    }

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @param clazz the fixture class
     * @throws InitializationError if the test class is malformed.
     */
    public SpringifiedConcordionRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        if (logger.isDebugEnabled()) {
            logger.debug("SpringifiedConcordionRunner constructor called with [" + clazz + "]");
        }
        ensureSpringRulesAreNotPresent(clazz);
        this.testContextManager = createTestContextManager(clazz);
    }

    /**
     * Create a new {@link TestContextManager} for the supplied test class.
     * <p>Can be overridden by subclasses.
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
     * Return a description suitable for an ignored test class if the test is disabled via {@code @IfProfileValue} at
     * the class-level, and otherwise delegate to the parent implementation.
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
     * Check whether the test is enabled in the current execution environment.
     * <p>This prevents classes with a non-matching {@code @IfProfileValue}
     * annotation from running altogether, even skipping the execution of {@code prepareTestInstance()} methods in
     * {@code TestExecutionListeners}.
     *
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
     * Wrap the {@link Statement} returned by the parent implementation with a {@code RunBeforeTestClassCallbacks}
     * statement, thus preserving the default JUnit functionality while adding support for the Spring TestContext
     * Framework.
     *
     * @see RunBeforeTestClassCallbacks
     */
    @Override
    protected Statement withBeforeClasses(Statement statement) {
        Statement junitBeforeClasses = super.withBeforeClasses(statement);
        return new RunBeforeTestClassCallbacks(junitBeforeClasses, getTestContextManager());
    }

    /**
     * Wrap the {@link Statement} returned by the parent implementation with a {@code RunAfterTestClassCallbacks}
     * statement, thus preserving the default JUnit functionality while adding support for the Spring TestContext
     * Framework.
     *
     * @see RunAfterTestClassCallbacks
     */
    @Override
    protected Statement withAfterClasses(Statement statement) {
        Statement junitAfterClasses = super.withAfterClasses(statement);
        return new RunAfterTestClassCallbacks(junitAfterClasses, getTestContextManager());
    }

    /**
     * Delegate to the parent implementation for creating the test instance and then allow the {@link
     * #getTestContextManager() TestContextManager} to prepare the test instance before returning it.
     *
     * @see TestContextManager#prepareTestInstance
     */
    @Override
    protected Object createTest() throws Exception {
        Object testInstance = super.createTest();
        getTestContextManager().prepareTestInstance(testInstance);
        return testInstance;
    }

    /**
     * Perform the same logic as {@link BlockJUnit4ClassRunner#runChild(FrameworkMethod, RunNotifier)}, except that
     * tests are determined to be <em>ignored</em> by {@link #isTestMethodIgnored(FrameworkMethod)}.
     */
    @Override
    protected void runChild(FrameworkMethod frameworkMethod, RunNotifier notifier) {
        Description description = describeChild(frameworkMethod);
        if (isTestMethodIgnored(frameworkMethod)) {
            notifier.fireTestIgnored(description);
        } else {
            Statement statement;
            try {
                statement = methodBlock(frameworkMethod);
            } catch (Throwable ex) {
                statement = new Fail(ex);
            }
            runLeaf(statement, description, notifier);
        }
    }

    /**
     * Similar to how SpringJUnit4ClassRunner has augmented BlockJUnit4ClassRunner except the callbacks relating to
     * timeout and repeat because FrameworkMethods in Concordion are not possible to be annotated.
     *
     * @see #methodInvoker(FrameworkMethod, Object)
     * @see #withBeforeTestExecutionCallbacks(FrameworkMethod, Object, Statement)
     * @see #withAfterTestExecutionCallbacks(FrameworkMethod, Object, Statement)
     * @see #possiblyExpectingExceptions(FrameworkMethod, Object, Statement)
     * @see #withBefores(FrameworkMethod, Object, Statement)
     * @see #withAfters(FrameworkMethod, Object, Statement)
     * @see #withRulesReflectively(FrameworkMethod, Object, Statement)
     */
    @Override
    protected Statement methodBlock(FrameworkMethod frameworkMethod) {
        Object testInstance;
        try {
            testInstance = new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return createTest();
                }
            }.run();
        } catch (Throwable ex) {
            return new Fail(ex);
        }

        Statement statement = methodInvoker(frameworkMethod, testInstance);
        statement = withBeforeTestExecutionCallbacks(frameworkMethod, testInstance, statement);
        statement = withAfterTestExecutionCallbacks(frameworkMethod, testInstance, statement);
        statement = possiblyExpectingExceptions(frameworkMethod, testInstance, statement);
        statement = withBefores(frameworkMethod, testInstance, statement);
        statement = withAfters(frameworkMethod, testInstance, statement);
        statement = withRulesReflectively(frameworkMethod, testInstance, statement);
        return statement;
    }

    /**
     * Invoke JUnit's private {@code withRules()} method using reflection.
     */
    private Statement withRulesReflectively(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
        Object result = ReflectionUtils.invokeMethod(withRulesMethod, this, frameworkMethod, testInstance, statement);
        Assert.state(result instanceof Statement, "withRules mismatch");
        return (Statement) result;
    }

    /**
     * Return {@code true} if {@link Ignore @Ignore} is present for the supplied {@linkplain FrameworkMethod test
     * method} or if the test method is disabled via {@code @IfProfileValue}.
     *
     * @see ProfileValueUtils#isTestEnabledInThisEnvironment(Method, Class)
     */
    protected boolean isTestMethodIgnored(FrameworkMethod frameworkMethod) {
        Method method = frameworkMethod.getMethod();
        return (method.isAnnotationPresent(Ignore.class) ||
                !ProfileValueUtils.isTestEnabledInThisEnvironment(method, getTestClass().getJavaClass()));
    }

    /**
     * Wrap the supplied {@link Statement} with a {@code RunBeforeTestExecutionCallbacks} statement, thus preserving the
     * default functionality while adding support for the Spring TestContext Framework.
     *
     * @see RunBeforeTestExecutionCallbacks
     */
    protected Statement withBeforeTestExecutionCallbacks(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
        return new RunBeforeTestExecutionCallbacks(statement, testInstance, frameworkMethod.getMethod(), getTestContextManager());
    }

    /**
     * Wrap the supplied {@link Statement} with a {@code RunAfterTestExecutionCallbacks} statement, thus preserving the
     * default functionality while adding support for the Spring TestContext Framework.
     *
     * @see RunAfterTestExecutionCallbacks
     */
    protected Statement withAfterTestExecutionCallbacks(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
        return new RunAfterTestExecutionCallbacks(statement, testInstance, frameworkMethod.getMethod(), getTestContextManager());
    }

    /**
     * Wrap the {@link Statement} returned by the parent implementation with a {@code RunBeforeTestMethodCallbacks}
     * statement, thus preserving the default functionality while adding support for the Spring TestContext Framework.
     *
     * @see RunBeforeTestMethodCallbacks
     */
    @Override
    protected Statement withBefores(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
        Statement junitBefores = super.withBefores(frameworkMethod, testInstance, statement);
        return new RunBeforeTestMethodCallbacks(junitBefores, testInstance, frameworkMethod.getMethod(), getTestContextManager());
    }

    /**
     * Wrap the {@link Statement} returned by the parent implementation with a {@code RunAfterTestMethodCallbacks}
     * statement, thus preserving the default functionality while adding support for the Spring TestContext Framework.
     *
     * @see RunAfterTestMethodCallbacks
     */
    @Override
    protected Statement withAfters(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
        Statement junitAfters = super.withAfters(frameworkMethod, testInstance, statement);
        return new RunAfterTestMethodCallbacks(junitAfters, testInstance, frameworkMethod.getMethod(), getTestContextManager());
    }

}
