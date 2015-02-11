/*
 * WebZ Server is a server that can serve web pages from various sources.
 * Copyright (C) 2013-2015  Oleksandr Tereschenko <http://www.terems.org/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.terems.webz.test.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import org.objenesis.ObjenesisHelper;
import org.testng.Assert;

/** TODO !!! describe !!! **/
public class WebzTestUtils {

	/** TODO !!! describe !!! **/
	public static <T> T assertNotNull(T object) {
		return assertNotNull(object, null);
	}

	/** TODO !!! describe !!! **/
	public static <T> T assertNotNull(T object, String message) {

		Assert.assertNotNull(object, message);

		return object;
	}

	/** TODO !!! describe !!! **/
	public static void assertInstanceOf(Object object, Class<?> expectedClass) {
		assertInstanceOf(object, expectedClass, null);
	}

	/** TODO !!! describe !!! **/
	public static void assertInstanceOf(Object object, Class<?> expectedClass, String message) {

		Class<?> objectClass = assertNotNull(object, message).getClass();

		if (!expectedClass.isAssignableFrom(objectClass)) {
			fail(object, objectClass + " is not assignable from " + expectedClass, message);
		}
	}

	/** TODO !!! describe !!! **/
	public static <T> T assertExceptionThrown(T object, Class<? extends Throwable> expectedExceptionClass) {
		return assertExceptionThrown(object, expectedExceptionClass, null);
	}

	/** TODO !!! describe !!! **/
	public static <T> T assertExceptionThrown(final T object, final Class<? extends Throwable> expectedExceptionClass, final String message) {

		assertNotNull(object, message);

		return createProxy(object.getClass(), new MethodHandler() {

			@Override
			public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {

				Throwable methodException = null;

				try {
					thisMethod.invoke(object, args);
				} catch (InvocationTargetException e) {
					methodException = e.getCause();
				}

				if (methodException == null || !expectedExceptionClass.isAssignableFrom(methodException.getClass())) {
					fail(object, "method " + thisMethod.getName() + "(...) was expected to throw " + expectedExceptionClass.getName()
							+ " but it "
							+ (methodException == null ? "did not" : "throwed " + methodException.getClass().getName() + " instead"),
							message);
				}

				return null;
			}

		});
	}

	private static void fail(Object object, String failure, String message) {
		fail("[object: " + object + "] " + failure, message);
	}

	private static void fail(String failure, String message) {
		if (message == null) {
			Assert.fail(failure);
		} else {
			Assert.fail(message + " " + failure);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T createProxy(Class<?> clazz, MethodHandler mh) {

		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(clazz);

		Class<T> proxyClass = factory.createClass();

		T proxy = ObjenesisHelper.newInstance(proxyClass);
		((Proxy) proxy).setHandler(mh);

		return proxy;
	}

}
