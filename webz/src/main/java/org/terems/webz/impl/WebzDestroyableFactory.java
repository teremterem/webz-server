package org.terems.webz.impl;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzDestroyable;
import org.terems.webz.WebzException;

public class WebzDestroyableFactory implements WebzDestroyable {

	private static final Logger LOG = LoggerFactory.getLogger(WebzDestroyableFactory.class);

	private static final String NULL_ENCOUNTERED_MSG = "null was encountered among " + WebzDestroyable.class.getSimpleName()
			+ " objects while destroying " + WebzDestroyableFactory.class.getSimpleName();
	private static final String ALREADY_DESTROYED_MSG = WebzDestroyableFactory.class.getSimpleName() + " is already destroyed";

	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	private volatile Queue<WebzDestroyable> destroyables = new ConcurrentLinkedQueue<>();

	/**
	 * <a href="http://ria101.wordpress.com/2011/12/12/concurrenthashmap-avoid-a-common-misuse/">ConcurrentHashMap – avoid a common
	 * misuse!</a>
	 **/
	private volatile ConcurrentMap<Class<? extends WebzDestroyable>, DestroyableWrapper> singletons = new ConcurrentHashMap<>();

	public <T extends WebzDestroyable> T newDestroyable(Class<T> destroyableClass) throws WebzException {

		Lock readLock = readWriteLock.readLock();
		try {
			readLock.lock();

			Queue<WebzDestroyable> destroyables = this.destroyables;
			if (destroyables == null) {
				throwFactoryDestroyed();
			}

			T destroyable = newDestroyable0(destroyableClass);
			destroyables.add(destroyable);

			return destroyable;

		} finally {
			readLock.unlock();
		}

	}

	/**
	 * <a
	 * href="http://stackoverflow.com/questions/17112504/with-double-checked-locking-does-a-put-to-a-volatile-concurrenthashmap-have-hap">
	 * With double-checked locking, does a put to a volatile ConcurrentHashMap have happens-before guarantee?</a>
	 **/
	public <T extends WebzDestroyable> T getDestroyableSingleton(Class<T> destroyableClass) throws WebzException {

		Lock readLock = readWriteLock.readLock();
		try {
			readLock.lock();

			ConcurrentMap<Class<? extends WebzDestroyable>, DestroyableWrapper> singletons = this.singletons;
			if (singletons == null) {
				throwFactoryDestroyed();
			}

			DestroyableWrapper wrapper = singletons.get(destroyableClass);
			if (wrapper == null) {

				DestroyableWrapper newWrapper = new DestroyableWrapper();
				wrapper = singletons.putIfAbsent(destroyableClass, newWrapper);

				if (wrapper == null) {
					wrapper = newWrapper;
				}
			}

			return initDestroyableWrapper(wrapper, destroyableClass);

		} finally {
			readLock.unlock();
		}
	}

	@Override
	public void destroy() {

		Lock writeLock = readWriteLock.writeLock();
		try {
			writeLock.lock();

			Queue<WebzDestroyable> destroyables = this.destroyables;
			ConcurrentMap<Class<? extends WebzDestroyable>, DestroyableWrapper> singletons = this.singletons;

			if (destroyables != null) {
				this.destroyables = null;

				for (WebzDestroyable destroyable : destroyables) {
					destroySafely(destroyable);
				}
			}

			if (singletons != null) {
				this.singletons = null;

				for (DestroyableWrapper wrapper : singletons.values()) {
					destroySafely(wrapper.destroyable);
				}
			}

		} finally {
			writeLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends WebzDestroyable> T initDestroyableWrapper(DestroyableWrapper wrapper, Class<T> destroyableClass)
			throws WebzException {

		WebzDestroyable destroyable = wrapper.destroyable;
		if (destroyable == null) {

			synchronized (wrapper) {

				destroyable = wrapper.destroyable;
				if (destroyable == null) {

					wrapper.destroyable = destroyable = newDestroyable0(destroyableClass);
				}
			}
		}
		return (T) destroyable;
	}

	private <T extends WebzDestroyable> T newDestroyable0(Class<T> destroyableClass) throws WebzException {

		try {
			return destroyableClass.newInstance();

		} catch (InstantiationException | IllegalAccessException e) {
			throw new WebzException(e);
		}
	}

	private void destroySafely(WebzDestroyable destroyable) {

		if (destroyable == null) {
			LOG.warn(NULL_ENCOUNTERED_MSG);

		} else {
			try {
				destroyable.destroy();

			} catch (Throwable th) {
				LOG.warn(th.getMessage(), th);
			}
		}
	}

	private void throwFactoryDestroyed() throws WebzException {
		throw new WebzException(ALREADY_DESTROYED_MSG);
	}

	private static class DestroyableWrapper {

		private volatile WebzDestroyable destroyable;
	}

}
