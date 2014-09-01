package org.terems.webz.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.terems.webz.WebzDestroyable;
import org.terems.webz.WebzException;

public class WebzDestroyableFactory implements WebzDestroyable {

	/**
	 * <a href="http://ria101.wordpress.com/2011/12/12/concurrenthashmap-avoid-a-common-misuse/">ConcurrentHashMap – avoid a common
	 * misuse!</a>
	 */
	private ConcurrentMap<Class<? extends WebzDestroyable>, WebzDestroyable> destroyableSingletons = new ConcurrentHashMap<>();

	private <T extends WebzDestroyable> T createDestroyable(Class<T> destroyableClass) throws WebzException {
		try {
			return destroyableClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new WebzException(e);
		}
	}

	/**
	 * <a href="http://stackoverflow.com/questions/7003239/double-checked-locking-with-concurrentmap">Double checked locking with
	 * ConcurrentMap</a> - yes, it is safe!
	 */
	public <T extends WebzDestroyable> T getDestroyableSingleton(Class<T> destroyableClass) throws WebzException {
		if (destroyableSingletons == null) {
			throw new WebzException("the factory is already destroyed");
		}

		// TODO TODO TODO

		return null;
	}

	@Override
	public void destroy() {

		// TODO TODO TODO
	}

}
