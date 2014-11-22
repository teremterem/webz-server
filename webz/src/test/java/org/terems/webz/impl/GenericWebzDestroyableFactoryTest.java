package org.terems.webz.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.terems.webz.test.util.WebzTestUtils.assertExceptionThrown;
import static org.terems.webz.test.util.WebzTestUtils.assertInstanceOf;
import static org.terems.webz.test.util.WebzTestUtils.assertNotNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.terems.webz.WebzDestroyable;
import org.terems.webz.WebzException;
import org.testng.annotations.Test;

public class GenericWebzDestroyableFactoryTest {

	private static final String WRONG_NUMBER_OF_UNIQUE_OBJECTS_MSG = "wrong number of unique objects created by factory(ies)";

	@Test
	public void testFactoryMethods() throws WebzException {

		GenericWebzObjectFactory factory = new GenericWebzObjectFactory();

		assertFactoryMethods(factory, null);
	}

	@Test
	public void testReturnedClasses() throws WebzException {

		GenericWebzObjectFactory factory = new GenericWebzObjectFactory();

		assertInstanceOf(factory.newDestroyable(DestroyableTestClass.class), DestroyableTestClass.class);
		assertInstanceOf(factory.newDestroyable(DestroyableTestClass.class.getName()), DestroyableTestClass.class);

		assertInstanceOf(factory.newDestroyable(AnotherDestroyableTestClass.class), AnotherDestroyableTestClass.class);
		assertInstanceOf(factory.newDestroyable(AnotherDestroyableTestClass.class.getName()), AnotherDestroyableTestClass.class);

		assertInstanceOf(factory.getDestroyableSingleton(DestroyableTestClass.class), DestroyableTestClass.class);
		assertInstanceOf(factory.getDestroyableSingleton(AnotherDestroyableTestClass.class), AnotherDestroyableTestClass.class);
		assertInstanceOf(factory.getDestroyableSingleton(DestroyableTestClass.class), DestroyableTestClass.class);

		assertInstanceOf(factory.getDestroyableSingleton(AnotherDestroyableTestClass.class.getName()), AnotherDestroyableTestClass.class);
		assertInstanceOf(factory.getDestroyableSingleton(DestroyableTestClass.class.getName()), DestroyableTestClass.class);
		assertInstanceOf(factory.getDestroyableSingleton(AnotherDestroyableTestClass.class.getName()), AnotherDestroyableTestClass.class);
	}

	@Test
	public void testIllegalArguments() throws WebzException {

		GenericWebzObjectFactory assertException = assertExceptionThrown(new GenericWebzObjectFactory(), WebzException.class);

		assertException.newDestroyable("fake1");

		assertException.getDestroyableSingleton("fake1");
		assertException.getDestroyableSingleton("fake2");
		assertException.getDestroyableSingleton("fake2");

		assertException.newDestroyable(HashMap.class.getName());

		assertException.getDestroyableSingleton(HashMap.class.getName());
		assertException.getDestroyableSingleton(Object.class.getName());
		assertException.getDestroyableSingleton(Object.class.getName());

		assertException.getDestroyableSingleton(Map.class.getName());

		assertException.newDestroyable(Map.class.getName());

		assertException.getDestroyableSingleton(WebzDestroyable.class);
		assertException.getDestroyableSingleton(WebzDestroyable.class.getName());

		assertException.newDestroyable(WebzDestroyable.class);
		assertException.newDestroyable(WebzDestroyable.class.getName());
	}

	@Test
	public void testFactoryDestroy() throws WebzException {

		GenericWebzObjectFactory factory = new GenericWebzObjectFactory();

		WebzDestroyable counterMock = mock(WebzDestroyable.class);

		GenericWebzObjectFactory subFactory2_1 = assertFactoryMethods(factory, counterMock);
		GenericWebzObjectFactory subFactory2_2 = assertFactoryMethods(factory, counterMock);
		GenericWebzObjectFactory subFactory2_3 = assertFactoryMethods(factory, counterMock);
		GenericWebzObjectFactory subFactory1_singleton = factory.getDestroyableSingleton(GenericWebzObjectFactory.class);

		factory.destroy();

		verify(counterMock, times(11 + 11 + 11 + 4)).destroy();

		assertFactoryDestroyed(subFactory2_1);
		assertFactoryDestroyed(subFactory2_2);
		assertFactoryDestroyed(subFactory2_3);
		assertFactoryDestroyed(subFactory1_singleton);
		assertFactoryDestroyed(factory);
	}

	@Test
	public void testExceptionDuringDestroy() throws WebzException {

		GenericWebzObjectFactory factory = new GenericWebzObjectFactory();

		WebzDestroyable counterMock = mock(WebzDestroyable.class);

		assertNotNull(factory.newDestroyable(ExceptionDuringDestroyTestClass.class)).init(counterMock);
		assertNotNull((AbstractDestroyableTestClass) factory.newDestroyable(ExceptionDuringDestroyTestClass.class.getName())).init(
				counterMock);

		assertNotNull(
				(AbstractDestroyableTestClass) factory.getDestroyableSingleton(AnotherExceptionDuringDestroyTestClass.class.getName()))
				.init(counterMock);
		assertNotNull(factory.getDestroyableSingleton(AnotherExceptionDuringDestroyTestClass.class)).init(counterMock);
		assertNotNull(factory.getDestroyableSingleton(ExceptionDuringDestroyTestClass.class)).init(counterMock);
		assertNotNull((AbstractDestroyableTestClass) factory.getDestroyableSingleton(ExceptionDuringDestroyTestClass.class.getName()))
				.init(counterMock);

		factory.destroy();

		verify(counterMock, times(4)).destroy();
	}

	// TODO test exceptions while creating objects as well ?

	private static final long SLOW_SINGLETON_CONSTRUCTOR_DELAY_MILLIS = 55;

	@Test
	public void tryToTestMultithreading() throws InterruptedException, WebzException {

		final int numOfSingletonThreads = 30;
		final int numOfNonSingletonThreads = 20;
		final int numOfNonSingletonRepeats = 50;

		GenericWebzObjectFactory factory = new GenericWebzObjectFactory();

		WebzDestroyable counterMock = mock(WebzDestroyable.class);

		List<SingletonsTestRunnable> singletonRunnables = new ArrayList<>(numOfSingletonThreads);
		List<NonSingletonsTestRunnable> nonSingletonRunnables = new ArrayList<>(numOfNonSingletonThreads);

		for (int i = 0; i < numOfSingletonThreads; i++) {
			singletonRunnables.add(new SingletonsTestRunnable(factory, counterMock));
		}
		for (int i = 0; i < numOfNonSingletonThreads; i++) {
			nonSingletonRunnables.add(new NonSingletonsTestRunnable(factory, counterMock, numOfNonSingletonRepeats));
		}

		List<Thread> threads = new ArrayList<>(numOfSingletonThreads + numOfNonSingletonThreads);
		for (AbstractFactoryTestRunnable runnable : singletonRunnables) {
			threads.add(new Thread(runnable));
		}
		for (AbstractFactoryTestRunnable runnable : nonSingletonRunnables) {
			threads.add(new Thread(runnable));
		}

		for (Thread thread : threads) {
			thread.start();
		}
		for (Thread thread : threads) {
			thread.join();
		}

		for (AbstractFactoryTestRunnable runnable : singletonRunnables) {
			assertRunnableDidNotFail(runnable);
		}
		for (AbstractFactoryTestRunnable runnable : nonSingletonRunnables) {
			assertRunnableDidNotFail(runnable);
		}

		int expectedNumberOfUniqueObjecta = 2 + 4 * numOfNonSingletonRepeats * numOfNonSingletonThreads;

		WebzDestroyable slowSingleton = assertNotNull(factory.getDestroyableSingleton(SlowDestroyableTestClass.class));
		WebzDestroyable anotherSlowSingleton = assertNotNull(factory.getDestroyableSingleton(AnotherSlowDestroyableTestClass.class));

		assertNotSame(anotherSlowSingleton, slowSingleton);
		for (SingletonsTestRunnable runnable : singletonRunnables) {

			assertSame(runnable.slow, slowSingleton);
			assertSame(runnable.sameSlow, slowSingleton);

			assertSame(runnable.anotherSlow, anotherSlowSingleton);
			assertSame(runnable.sameAnotherSlow, anotherSlowSingleton);
		}

		Set<WebzDestroyable> uniqueObjects = new HashSet<>(expectedNumberOfUniqueObjecta, 1);
		// for classes that do not override Object's implementation of hashCode() and equals() HashSet will work like IdentityHashSet...
		uniqueObjects.add(slowSingleton);
		uniqueObjects.add(anotherSlowSingleton);
		for (NonSingletonsTestRunnable runnable : nonSingletonRunnables) {
			uniqueObjects.addAll(runnable.destroyables);
		}
		assertEquals(uniqueObjects.size(), expectedNumberOfUniqueObjecta, WRONG_NUMBER_OF_UNIQUE_OBJECTS_MSG);

		factory.destroy();

		verify(counterMock, times(expectedNumberOfUniqueObjecta)).destroy();
	}

	// ~ testFactoryMethods() related stuff ~

	/**
	 * In total, 11 new objects and 4 singletons that wrap {@code counterMock} should be created in this method using {@code factory} as a
	 * root factory. This means that if this method is called once for certain {@code factory} then 11+4=15 objects that wrap
	 * {@code counterMock} should be created; if this method is called twice for certain {@code factory} then 11+11+4=26 objects that wrap
	 * {@code counterMock} should be created and so on.
	 * 
	 * @return non-singleton sub-factory that was created in this method (singleton sub-factory can be fetched directly from
	 *         {@code topFactory})
	 */
	private GenericWebzObjectFactory assertFactoryMethods(GenericWebzObjectFactory factory, WebzDestroyable counterMock)
			throws WebzException {

		GenericWebzObjectFactory subFactory1_singleton = factory.getDestroyableSingleton(GenericWebzObjectFactory.class);
		GenericWebzObjectFactory subFactory2 = factory.newDestroyable(GenericWebzObjectFactory.class);
		// two sub-factories (singleton and non-singleton) created
		assertNotNull(subFactory1_singleton);
		assertNotNull(subFactory2);
		assertNotSame(subFactory2, subFactory1_singleton);

		WebzDestroyable top1 = assertNotNull(factory.newDestroyable(DestroyableTestClass.class)).init(counterMock);
		WebzDestroyable top2_singleton = assertNotNull(factory.getDestroyableSingleton(DestroyableTestClass.class)).init(counterMock);
		// 1 new object + 1 new DestroyableClass singleton created

		WebzDestroyable top3 = assertNotNull(factory.newDestroyable(DestroyableTestClass.class)).init(counterMock);
		WebzDestroyable top2_sameSingleton = assertNotNull(factory.getDestroyableSingleton(DestroyableTestClass.class)).init(counterMock);
		// 1 new object created
		assertSame(top2_sameSingleton, top2_singleton);

		WebzDestroyable top4 = assertNotNull(factory.newDestroyable(DestroyableTestClass.class)).init(counterMock);
		WebzDestroyable top5_singleton = assertNotNull(factory.getDestroyableSingleton(AnotherDestroyableTestClass.class))
				.init(counterMock);
		// 1 new object + 1 new AnotherDestroyableClass singleton created

		WebzDestroyable sub6 = assertNotNull(subFactory2.newDestroyable(DestroyableTestClass.class)).init(counterMock);
		WebzDestroyable sub7 = assertNotNull(subFactory2.newDestroyable(DestroyableTestClass.class)).init(counterMock);
		WebzDestroyable sub8 = assertNotNull(subFactory2.newDestroyable(AnotherDestroyableTestClass.class)).init(counterMock);
		WebzDestroyable sub9_singleton = assertNotNull(subFactory2.getDestroyableSingleton(DestroyableTestClass.class)).init(counterMock);
		WebzDestroyable sub9_sameSingleton = assertNotNull(subFactory2.getDestroyableSingleton(DestroyableTestClass.class)).init(
				counterMock);
		WebzDestroyable sub10_singleton = assertNotNull(subFactory2.getDestroyableSingleton(AnotherDestroyableTestClass.class)).init(
				counterMock);
		// 5 new objects created (NON-SINGLETON sub-factory was used here!)
		assertSame(sub9_sameSingleton, sub9_singleton);

		WebzDestroyable sub11 = assertNotNull(subFactory1_singleton.newDestroyable(DestroyableTestClass.class)).init(counterMock);
		WebzDestroyable sub12 = assertNotNull(subFactory1_singleton.newDestroyable(DestroyableTestClass.class)).init(counterMock);
		WebzDestroyable sub13 = assertNotNull(
				(AbstractDestroyableTestClass) subFactory1_singleton.newDestroyable(AnotherDestroyableTestClass.class.getName())).init(
				counterMock);
		WebzDestroyable sub14_singleton = assertNotNull(subFactory1_singleton.getDestroyableSingleton(DestroyableTestClass.class)).init(
				counterMock);
		// 3 new objects + 1 new DestroyableClass singleton created (using singleton sub-factory)

		GenericWebzObjectFactory subFactory1_sameSingleton = factory.getDestroyableSingleton(GenericWebzObjectFactory.class
				.getName());
		// this singleton sub-factory exists already
		assertSame(subFactory1_sameSingleton, subFactory1_singleton);

		WebzDestroyable sub14_sameSingleton = assertNotNull(subFactory1_sameSingleton.getDestroyableSingleton(DestroyableTestClass.class))
				.init(counterMock);
		WebzDestroyable sub15_singleton = assertNotNull(
				(AbstractDestroyableTestClass) subFactory1_sameSingleton.getDestroyableSingleton(AnotherDestroyableTestClass.class
						.getName())).init(counterMock);
		// 1 new DestroyableClass singleton created (using singleton sub-factory)
		assertSame(sub14_sameSingleton, sub14_singleton);

		Set<WebzDestroyable> uniqueObjects = new HashSet<>();
		// for classes that do not override Object's implementation of hashCode() and equals() HashSet will work like IdentityHashSet...
		uniqueObjects.add(top1);
		uniqueObjects.add(top2_singleton);
		uniqueObjects.add(top3);
		uniqueObjects.add(top2_sameSingleton);
		uniqueObjects.add(top4);
		uniqueObjects.add(top5_singleton);
		uniqueObjects.add(sub6);
		uniqueObjects.add(sub7);
		uniqueObjects.add(sub8);
		uniqueObjects.add(sub9_singleton);
		uniqueObjects.add(sub9_sameSingleton);
		uniqueObjects.add(sub10_singleton);
		uniqueObjects.add(sub11);
		uniqueObjects.add(sub12);
		uniqueObjects.add(sub13);
		uniqueObjects.add(sub14_singleton);
		uniqueObjects.add(sub14_sameSingleton);
		uniqueObjects.add(sub15_singleton);
		assertEquals(uniqueObjects.size(), 11 + 4, WRONG_NUMBER_OF_UNIQUE_OBJECTS_MSG);

		return subFactory2;
	}

	public static abstract class AbstractDestroyableTestClass implements WebzDestroyable {

		private WebzDestroyable mock;

		public AbstractDestroyableTestClass init(WebzDestroyable mock) {
			this.mock = mock;
			return this;
		}

		@Override
		public void destroy() {
			if (mock != null) {
				mock.destroy();
			}
		}
	}

	public static class DestroyableTestClass extends AbstractDestroyableTestClass {
	}

	public static class AnotherDestroyableTestClass extends AbstractDestroyableTestClass {
	}

	// ~ testFactoryDestroy() related stuff ~

	private void assertFactoryDestroyed(GenericWebzObjectFactory factory) throws WebzException {

		GenericWebzObjectFactory assertException = assertExceptionThrown(factory, WebzException.class);

		assertException.newDestroyable(DestroyableTestClass.class.getName());
		assertException.newDestroyable(DestroyableTestClass.class);

		assertException.getDestroyableSingleton(DestroyableTestClass.class.getName());
		assertException.getDestroyableSingleton(DestroyableTestClass.class);

		assertException.newDestroyable(AnotherDestroyableTestClass.class);
		assertException.newDestroyable(AnotherDestroyableTestClass.class.getName());

		assertException.getDestroyableSingleton(AnotherDestroyableTestClass.class);
		assertException.getDestroyableSingleton(AnotherDestroyableTestClass.class.getName());
	}

	// ~ testExceptionDuringDestroy() related stuff ~

	public static class ExceptionDuringDestroyTestClass extends AbstractDestroyableTestClass {
		@Override
		public void destroy() {
			super.destroy();

			throw new RuntimeException();
		}
	}

	public static class AnotherExceptionDuringDestroyTestClass extends ExceptionDuringDestroyTestClass {
	}

	// ~ tryToTestMultithreading() related stuff ~

	private void assertRunnableDidNotFail(AbstractFactoryTestRunnable runnable) throws AssertionError {

		if (runnable.exception != null) {
			throw new AssertionError("one of the threads failed with the following exception: " + runnable.exception, runnable.exception);
		}
	}

	private abstract class AbstractFactoryTestRunnable implements Runnable {

		public Throwable exception;

		protected GenericWebzObjectFactory factory;
		protected WebzDestroyable counterMock;

		public abstract void runTest() throws Throwable;

		public AbstractFactoryTestRunnable(GenericWebzObjectFactory factory, WebzDestroyable counterMock) {

			this.factory = factory;
			this.counterMock = counterMock;
		}

		@Override
		public void run() {

			try {
				runTest();

			} catch (Throwable th) {
				exception = th;
				throw new RuntimeException(th);
			}
		}
	}

	private class SingletonsTestRunnable extends AbstractFactoryTestRunnable {

		public WebzDestroyable slow;
		public WebzDestroyable sameSlow;
		public WebzDestroyable anotherSlow;
		public WebzDestroyable sameAnotherSlow;

		public SingletonsTestRunnable(GenericWebzObjectFactory factory, WebzDestroyable counterMock) {
			super(factory, counterMock);
		}

		@Override
		public void runTest() throws WebzException {

			slow = assertNotNull(factory.getDestroyableSingleton(SlowDestroyableTestClass.class)).init(counterMock);
			anotherSlow = assertNotNull(
					(AbstractDestroyableTestClass) factory.getDestroyableSingleton(AnotherSlowDestroyableTestClass.class.getName())).init(
					counterMock);

			sameSlow = assertNotNull(
					(AbstractDestroyableTestClass) factory.getDestroyableSingleton(SlowDestroyableTestClass.class.getName())).init(
					counterMock);
			sameAnotherSlow = assertNotNull(factory.getDestroyableSingleton(AnotherSlowDestroyableTestClass.class)).init(counterMock);
		}
	}

	private class NonSingletonsTestRunnable extends AbstractFactoryTestRunnable {

		public List<WebzDestroyable> destroyables;

		private int numOfRepeats;

		public NonSingletonsTestRunnable(GenericWebzObjectFactory factory, WebzDestroyable counterMock, int numOfRepeats) {

			super(factory, counterMock);

			this.numOfRepeats = numOfRepeats;
			this.destroyables = new ArrayList<>(numOfRepeats * 4);
		}

		@Override
		public void runTest() throws WebzException {

			for (int i = 0; i < numOfRepeats; i++) {

				destroyables.add(assertNotNull(
						(AbstractDestroyableTestClass) factory.newDestroyable(AnotherDestroyableTestClass.class.getName())).init(
						counterMock));
				destroyables.add(assertNotNull(factory.newDestroyable(AnotherDestroyableTestClass.class)).init(counterMock));

				destroyables.add(assertNotNull(factory.newDestroyable(DestroyableTestClass.class)).init(counterMock));
				destroyables.add(assertNotNull((AbstractDestroyableTestClass) factory.newDestroyable(DestroyableTestClass.class.getName()))
						.init(counterMock));
			}
		}
	}

	public static abstract class AbstractSlowDestroyableTestClass extends AbstractDestroyableTestClass {

		public AbstractSlowDestroyableTestClass() throws InterruptedException {
			Thread.sleep(SLOW_SINGLETON_CONSTRUCTOR_DELAY_MILLIS);
		}
	}

	public static class SlowDestroyableTestClass extends AbstractSlowDestroyableTestClass {

		public SlowDestroyableTestClass() throws InterruptedException {
		}
	}

	public static class AnotherSlowDestroyableTestClass extends AbstractSlowDestroyableTestClass {

		public AnotherSlowDestroyableTestClass() throws InterruptedException {
		}
	}

}
