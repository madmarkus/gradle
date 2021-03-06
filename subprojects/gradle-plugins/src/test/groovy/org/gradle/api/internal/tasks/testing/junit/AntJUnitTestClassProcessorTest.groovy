/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.gradle.api.internal.tasks.testing.junit

import junit.framework.TestCase
import org.gradle.api.internal.tasks.testing.TestCompleteEvent
import org.gradle.api.internal.tasks.testing.TestDescriptorInternal
import org.gradle.api.internal.tasks.testing.TestResultProcessor
import org.gradle.api.internal.tasks.testing.TestStartEvent
import org.gradle.api.testing.fabric.TestClassRunInfo
import org.gradle.util.JUnit4GroovyMockery
import org.gradle.util.LongIdGenerator
import org.gradle.util.TemporaryFolder
import org.jmock.integration.junit4.JMock
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runner.Runner
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

@RunWith(JMock.class)
class AntJUnitTestClassProcessorTest {
    private final JUnit4GroovyMockery context = new JUnit4GroovyMockery()
    @Rule public final TemporaryFolder tmpDir = new TemporaryFolder();
    private final TestResultProcessor resultProcessor = context.mock(TestResultProcessor.class);
    private final AntJUnitTestClassProcessor processor = new AntJUnitTestClassProcessor(tmpDir.dir, new LongIdGenerator());

    @Test
    public void executesATestClass() {
        context.checking {
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal suite ->
                assertThat(suite.id, equalTo(1L))
                assertThat(suite.name, equalTo(ATestClass.class.name))
                assertThat(suite.className, equalTo(ATestClass.class.name))
            }
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal test, TestStartEvent event ->
                assertThat(test.id, equalTo(2L))
                assertThat(test.name, equalTo('ok'))
                assertThat(test.className, equalTo(ATestClass.class.name))
            }
            one(resultProcessor).completed(withParam(equalTo(2L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
            one(resultProcessor).completed(withParam(equalTo(1L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
        }

        processor.startProcessing(resultProcessor);
        processor.processTestClass(testClass(ATestClass.class));
        processor.endProcessing();
    }

    @Test
    public void executesAJUnit3TestClass() {
        context.checking {
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal suite ->
                assertThat(suite.name, equalTo(AJunit3TestClass.class.name))
                assertThat(suite.className, equalTo(AJunit3TestClass.class.name))
            }
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal test ->
                assertThat(test.name, equalTo('testOk'))
                assertThat(test.className, equalTo(AJunit3TestClass.class.name))
            }
            one(resultProcessor).completed(withParam(equalTo(2L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
            one(resultProcessor).completed(withParam(equalTo(1L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
        }

        processor.startProcessing(resultProcessor);
        processor.processTestClass(testClass(AJunit3TestClass.class));
        processor.endProcessing();
    }

    @Test
    public void executesMultipleTestClasses() {
        context.checking {
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal suite ->
                assertThat(suite.id, equalTo(1L))
                assertThat(suite.name, equalTo(ATestClass.class.name))
                assertThat(suite.className, equalTo(ATestClass.class.name))
            }
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal test, TestStartEvent event ->
                assertThat(test.id, equalTo(2L))
                assertThat(test.name, equalTo('ok'))
                assertThat(test.className, equalTo(ATestClass.class.name))
            }
            one(resultProcessor).completed(withParam(equalTo(2L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
            one(resultProcessor).completed(withParam(equalTo(1L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal suite ->
                assertThat(suite.id, equalTo(3L))
                assertThat(suite.name, equalTo(AJunit3TestClass.class.name))
                assertThat(suite.className, equalTo(AJunit3TestClass.class.name))
            }
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal test, TestStartEvent event ->
                assertThat(test.id, equalTo(4L))
                assertThat(test.name, equalTo('testOk'))
                assertThat(test.className, equalTo(AJunit3TestClass.class.name))
            }
            one(resultProcessor).completed(withParam(equalTo(4L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
            one(resultProcessor).completed(withParam(equalTo(3L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
        }

        processor.startProcessing(resultProcessor);
        processor.processTestClass(testClass(ATestClass.class));
        processor.processTestClass(testClass(AJunit3TestClass.class));
        processor.endProcessing();
    }

    @Test
    public void executesATestClassWithRunWithAnnotation() {
        context.checking {
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal suite ->
                assertThat(suite.id, equalTo(1L))
                assertThat(suite.name, equalTo(ATestClassWithRunner.class.name))
                assertThat(suite.className, equalTo(ATestClassWithRunner.class.name))
            }
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal test ->
                assertThat(test.id, equalTo(2L))
                assertThat(test.name, equalTo('broken'))
                assertThat(test.className, equalTo(ATestClassWithRunner.class.name))
            }
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal test ->
                assertThat(test.id, equalTo(3L))
                assertThat(test.name, equalTo('ok'))
                assertThat(test.className, equalTo(ATestClassWithRunner.class.name))
            }
            one(resultProcessor).completed(withParam(equalTo(3L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
            one(resultProcessor).addFailure(2L, CustomRunner.failure)
            one(resultProcessor).completed(withParam(equalTo(2L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
            one(resultProcessor).completed(withParam(equalTo(1L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
        }

        processor.startProcessing(resultProcessor);
        processor.processTestClass(testClass(ATestClassWithRunner.class));
        processor.endProcessing();
    }

    @Test
    public void executesATestClassWithASuiteMethod() {
        context.checking {
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal suite ->
                assertThat(suite.name, equalTo(ATestClassWithSuiteMethod.class.name))
                assertThat(suite.className, equalTo(ATestClassWithSuiteMethod.class.name))
            }
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal test ->
                assertThat(test.id, equalTo(2L))
                assertThat(test.name, equalTo('testOk'))
                assertThat(test.className, equalTo(AJunit3TestClass.class.name))
            }
            one(resultProcessor).completed(withParam(equalTo(2L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal test ->
                assertThat(test.id, equalTo(3L))
                assertThat(test.name, equalTo('testOk'))
                assertThat(test.className, equalTo(AJunit3TestClass.class.name))
            }
            one(resultProcessor).completed(withParam(equalTo(3L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
            one(resultProcessor).completed(withParam(equalTo(1L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
        }

        processor.startProcessing(resultProcessor);
        processor.processTestClass(testClass(ATestClassWithSuiteMethod.class));
        processor.endProcessing();
    }

    @Test
    public void executesATestClassWithBrokenConstructor() {
        context.checking {
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal suite ->
                assertThat(suite.name, equalTo(ATestClassWithBrokenConstructor.class.name))
            }
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal test ->
                assertThat(test.id, equalTo(2L))
                assertThat(test.name, equalTo('test'))
                assertThat(test.className, equalTo(ATestClassWithBrokenConstructor.class.name))
            }
            one(resultProcessor).addFailure(2L, ATestClassWithBrokenConstructor.failure)
            one(resultProcessor).completed(withParam(equalTo(2L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
            one(resultProcessor).completed(withParam(notNullValue()), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
        }

        processor.startProcessing(resultProcessor);
        processor.processTestClass(testClass(ATestClassWithBrokenConstructor.class));
        processor.endProcessing();
    }

    @Test
    public void executesATestClassWithBrokenSetup() {
        context.checking {
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal suite ->
                assertThat(suite.name, equalTo(ATestClassWithBrokenSetup.class.name))
            }
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal test ->
                assertThat(test.name, equalTo('test'))
                assertThat(test.className, equalTo(ATestClassWithBrokenSetup.class.name))
            }
            one(resultProcessor).addFailure(2L, ATestClassWithBrokenSetup.failure)
            one(resultProcessor).completed(withParam(equalTo(2L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
            one(resultProcessor).completed(withParam(equalTo(1L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
        }

        processor.startProcessing(resultProcessor);
        processor.processTestClass(testClass(ATestClassWithBrokenSetup.class));
        processor.endProcessing();
    }

    @Test
    public void executesATestClassWithRunnerThatCannotBeConstructed() {
        context.checking {
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal suite ->
                assertThat(suite.name, equalTo(ATestClassWithUnconstructableRunner.class.name))
            }
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal test ->
                assertThat(test.name, equalTo('initializationError'))
                assertThat(test.className, equalTo(ATestClassWithUnconstructableRunner.class.name))
            }
            one(resultProcessor).addFailure(2L, CustomRunnerWithBrokenConstructor.failure)
            one(resultProcessor).completed(withParam(equalTo(2L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
            one(resultProcessor).completed(withParam(equalTo(1L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
        }

        processor.startProcessing(resultProcessor);
        processor.processTestClass(testClass(ATestClassWithUnconstructableRunner.class));
        processor.endProcessing();
    }

    @Test
    public void executesATestClassWhichCannotBeLoaded() {
        String testClassName = 'org.gradle.api.internal.tasks.testing.junit.ATestClassWhichCannotBeLoaded'

        context.checking {
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal suite ->
                assertThat(suite.name, equalTo(testClassName))
            }
            one(resultProcessor).started(withParam(notNullValue()), withParam(notNullValue()))
            will { TestDescriptorInternal test ->
                assertThat(test.name, equalTo('initializationError'))
                assertThat(test.className, equalTo(testClassName))
            }
            one(resultProcessor).completed(withParam(equalTo(2L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, instanceOf(NoClassDefFoundError.class))
            }
            one(resultProcessor).completed(withParam(equalTo(1L)), withParam(notNullValue()))
            will { id, TestCompleteEvent event ->
                assertThat(event.resultType, nullValue())
                assertThat(event.failure, nullValue())
            }
        }

        processor.startProcessing(resultProcessor);
        processor.processTestClass(testClass(testClassName));
        processor.endProcessing();
    }

    private TestClassRunInfo testClass(Class<?> type) {
        return testClass(type.name)
    }

    private TestClassRunInfo testClass(String testClassName) {
        TestClassRunInfo runInfo = context.mock(TestClassRunInfo.class, testClassName)
        context.checking {
            allowing(runInfo).getTestClassName()
            will(returnValue(testClassName))
        }
        return runInfo;
    }
}

public static class ATestClass {
    @Test
    public void ok() {
    }

    @Test @Ignore
    public void ignored() {
    }
}

public static class ATestClassWithBrokenConstructor {
    static RuntimeException failure = new RuntimeException()

    def ATestClassWithBrokenConstructor() {
        throw failure.fillInStackTrace()
    }

    @Test
    public void test() {
    }
}

public static class ATestClassWithBrokenSetup {
    static RuntimeException failure = new RuntimeException()

    @Before
    public void setup() {
        throw failure.fillInStackTrace()
    }

    @Test
    public void test() {
    }
}

public static class AJunit3TestClass extends TestCase {
    public void testOk() {
    }
}

public static class ATestClassWithSuiteMethod {
    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AJunit3TestClass.class, AJunit3TestClass.class);
    }
}

@RunWith(CustomRunner.class)
public static class ATestClassWithRunner {}

public static class CustomRunner extends Runner {
    static RuntimeException failure = new RuntimeException('broken')
    Class<?> type

    def CustomRunner(Class<?> type) {
        this.type = type
    }

    @Override
    public Description getDescription() {
        Description description = Description.createSuiteDescription(type)
        description.addChild(Description.createTestDescription(type, 'ok1'))
        description.addChild(Description.createTestDescription(type, 'ok2'))
        return description
    }

    @Override
    public void run(RunNotifier runNotifier) {
        // Run tests in 'parallel'
        Description test1 = Description.createTestDescription(type, 'broken')
        Description test2 = Description.createTestDescription(type, 'ok')
        runNotifier.fireTestStarted(test1)
        runNotifier.fireTestStarted(test2)
        runNotifier.fireTestFailure(new Failure(test1, failure.fillInStackTrace()))
        runNotifier.fireTestFinished(test2)
        runNotifier.fireTestFinished(test1)
    }
}

@RunWith(CustomRunnerWithBrokenConstructor.class)
public static class ATestClassWithUnconstructableRunner {}

public static class CustomRunnerWithBrokenConstructor extends Runner {
    static RuntimeException failure = new RuntimeException()

    def CustomRunnerWithBrokenConstructor(Class<?> type) {
        throw failure.fillInStackTrace()
    }

    Description getDescription() {
        throw new UnsupportedOperationException();
    }

    void run(RunNotifier notifier) {
        throw new UnsupportedOperationException();
    }
}

public static class ATestClassWhichCannotBeLoaded {
    static {
        throw new NoClassDefFoundError()
    }
}
