package org.terems.webz.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.terems.webz.test.util.WebzTestUtils.assertExceptionThrown;
import static org.terems.webz.test.util.WebzTestUtils.assertInstanceOf;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.terems.webz.WebzDestroyable;
import org.terems.webz.WebzException;
import org.testng.annotations.Test;

public class WebzDestroyableFactoryTest {

	@Test
	public void testFactoryMethods() throws WebzException {

		WebzDestroyableFactory factory = new WebzDestroyableFactory();

		assertFactoryMethods(factory, null);
	}

	@Test
	public void testReturnedClasses() throws WebzException {

		WebzDestroyableFactory factory = new WebzDestroyableFactory();

		assertInstanceOf(factory.newDestroyable(DestroyableClass.class), DestroyableClass.class);
		assertInstanceOf(factory.newDestroyable(AnotherDestroyableClass.class), AnotherDestroyableClass.class);

		assertInstanceOf(factory.getDestroyableSingleton(DestroyableClass.class), DestroyableClass.class);
		assertInstanceOf(factory.getDestroyableSingleton(AnotherDestroyableClass.class), AnotherDestroyableClass.class);
		assertInstanceOf(factory.getDestroyableSingleton(DestroyableClass.class), DestroyableClass.class);
	}

	@Test
	public void testFactoryDestroy() throws WebzException {

		WebzDestroyableFactory factory = new WebzDestroyableFactory();

		WebzDestroyable counterMock = mock(WebzDestroyable.class);

		WebzDestroyableFactory subFactory2_1 = assertFactoryMethods(factory, counterMock);
		WebzDestroyableFactory subFactory2_2 = assertFactoryMethods(factory, counterMock);
		WebzDestroyableFactory subFactory2_3 = assertFactoryMethods(factory, counterMock);
		WebzDestroyableFactory subFactory1_singleton = factory.getDestroyableSingleton(WebzDestroyableFactory.class);

		factory.destroy();

		verify(counterMock, times(11 + 11 + 11 + 4)).destroy();

		assertFactoryDestroyed(subFactory2_1);
		assertFactoryDestroyed(subFactory2_2);
		assertFactoryDestroyed(subFactory2_3);
		assertFactoryDestroyed(subFactory1_singleton);
		assertFactoryDestroyed(factory);
	}

	@Test
	public void tryToTestMultithreading() throws InterruptedException {

		final int numOfRepeats = 20;
		final int numOfThreads = 10;

		WebzDestroyableFactory factory = new WebzDestroyableFactory();

		WebzDestroyable counterMock = mock(WebzDestroyable.class);

		Runnable runnable = null; // new SingletonsAssertionRunnable(factory, counterMock, numOfRepeats);

		List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < numOfThreads; i++) {
			threads.add(new Thread(runnable));
		}
		for (Thread thread : threads) {
			thread.start();
		}
		for (Thread thread : threads) {
			thread.join();
		}

		// TODO check for exceptions in threads
		// TODO find a way to check if duplicate singletons were not created

		factory.destroy();

		// verify(counterMock, times(11 * numOfRepeats * numOfThreads + 4)).destroy();
	}

	private static final long CONSTRUCTOR_DELAY_MILLIS = 100;

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
	private WebzDestroyableFactory assertFactoryMethods(WebzDestroyableFactory factory, WebzDestroyable counterMock) throws WebzException {

		WebzDestroyableFactory subFactory1_singleton = factory.getDestroyableSingleton(WebzDestroyableFactory.class);
		WebzDestroyableFactory subFactory2 = factory.newDestroyable(WebzDestroyableFactory.class);
		// two sub-factories (singleton and non-singleton) created
		assertNotNull(subFactory1_singleton);
		assertNotNull(subFactory2);
		assertNotSame(subFactory2, subFactory1_singleton);

		WebzDestroyable top1 = factory.newDestroyable(DestroyableClass.class).init(counterMock);
		WebzDestroyable top2_singleton = factory.getDestroyableSingleton(DestroyableClass.class).init(counterMock);
		// 1 new object + 1 new DestroyableClass singleton created

		WebzDestroyable top3 = factory.newDestroyable(DestroyableClass.class).init(counterMock);
		WebzDestroyable top2_sameSingleton = factory.getDestroyableSingleton(DestroyableClass.class).init(counterMock);
		// 1 new object created
		assertSame(top2_sameSingleton, top2_singleton);

		WebzDestroyable top4 = factory.newDestroyable(DestroyableClass.class).init(counterMock);
		WebzDestroyable top5_singleton = factory.getDestroyableSingleton(AnotherDestroyableClass.class).init(counterMock);
		// 1 new object + 1 new AnotherDestroyableClass singleton created

		WebzDestroyable sub6 = subFactory2.newDestroyable(DestroyableClass.class).init(counterMock);
		WebzDestroyable sub7 = subFactory2.newDestroyable(DestroyableClass.class).init(counterMock);
		WebzDestroyable sub8 = subFactory2.newDestroyable(AnotherDestroyableClass.class).init(counterMock);
		WebzDestroyable sub9_singleton = subFactory2.getDestroyableSingleton(DestroyableClass.class).init(counterMock);
		WebzDestroyable sub9_sameSingleton = subFactory2.getDestroyableSingleton(DestroyableClass.class).init(counterMock);
		WebzDestroyable sub10_singleton = subFactory2.getDestroyableSingleton(AnotherDestroyableClass.class).init(counterMock);
		// 5 new objects created (NON-SINGLETON sub-factory was used here!)
		assertSame(sub9_sameSingleton, sub9_singleton);

		WebzDestroyable sub11 = subFactory1_singleton.newDestroyable(DestroyableClass.class).init(counterMock);
		WebzDestroyable sub12 = subFactory1_singleton.newDestroyable(DestroyableClass.class).init(counterMock);
		WebzDestroyable sub13 = subFactory1_singleton.newDestroyable(AnotherDestroyableClass.class).init(counterMock);
		WebzDestroyable sub14_singleton = subFactory1_singleton.getDestroyableSingleton(DestroyableClass.class).init(counterMock);
		// 3 new objects + 1 new DestroyableClass singleton created (using singleton sub-factory)

		WebzDestroyableFactory subFactory1_sameSingleton = factory.getDestroyableSingleton(WebzDestroyableFactory.class);
		// this singleton sub-factory exists already
		assertSame(subFactory1_sameSingleton, subFactory1_singleton);

		WebzDestroyable sub14_sameSingleton = subFactory1_sameSingleton.getDestroyableSingleton(DestroyableClass.class).init(counterMock);
		WebzDestroyable sub15_singleton = subFactory1_sameSingleton.getDestroyableSingleton(AnotherDestroyableClass.class)
				.init(counterMock);
		// 1 new DestroyableClass singleton created (using singleton sub-factory)
		assertSame(sub14_sameSingleton, sub14_singleton);

		Set<WebzDestroyable> uniqueObjects = new HashSet<>();
		// for DestroyableClass and AnotherDestroyableClass objects HashSet will work like IdentityHashSet...
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
		assertEquals(uniqueObjects.size(), 11 + 4, "wrong number of unique objects created by factories");

		return subFactory2;
	}

	public static abstract class AbstractDestroyableClass implements WebzDestroyable {

		private WebzDestroyable mock;

		public AbstractDestroyableClass init(WebzDestroyable mock) {
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

	public static class DestroyableClass extends AbstractDestroyableClass {
	}

	public static class AnotherDestroyableClass extends AbstractDestroyableClass {
	}

	// ~ testFactoryDestroy() related stuff ~

	private void assertFactoryDestroyed(WebzDestroyableFactory factory) throws WebzException {

		WebzDestroyableFactory assertException = assertExceptionThrown(factory, WebzException.class);

		assertException.newDestroyable(DestroyableClass.class);
		assertException.getDestroyableSingleton(DestroyableClass.class);

		assertException.newDestroyable(AnotherDestroyableClass.class);
		assertException.getDestroyableSingleton(AnotherDestroyableClass.class);
	}

	// ~ tryToTestMultithreading() related stuff ~

	private class SingletonsAssertionRunnable implements Runnable {

		public WebzDestroyable slow;
		public WebzDestroyable anotherSlow;
		public WebzDestroyable sameAnotherSlow;

		private WebzDestroyableFactory factory;
		private WebzDestroyable counterMock;

		public SingletonsAssertionRunnable(WebzDestroyableFactory factory, WebzDestroyable counterMock) {
			this.factory = factory;
			this.counterMock = counterMock;
		}

		@Override
		public void run() {
			try {
				slow = factory.getDestroyableSingleton(SlowDestroyableClass.class).init(counterMock);
				anotherSlow = factory.getDestroyableSingleton(AnotherSlowDestroyableClass.class).init(counterMock);
				sameAnotherSlow = factory.getDestroyableSingleton(AnotherSlowDestroyableClass.class).init(counterMock);
			} catch (WebzException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static abstract class AbstractSlowDestroyableClass extends AbstractDestroyableClass {
		public AbstractSlowDestroyableClass() throws InterruptedException {
			Thread.sleep(CONSTRUCTOR_DELAY_MILLIS);
		}
	}

	public static class SlowDestroyableClass extends AbstractSlowDestroyableClass {
		public SlowDestroyableClass() throws InterruptedException {
		}
	}

	public static class AnotherSlowDestroyableClass extends AbstractSlowDestroyableClass {
		public AnotherSlowDestroyableClass() throws InterruptedException {
		}
	}

}
