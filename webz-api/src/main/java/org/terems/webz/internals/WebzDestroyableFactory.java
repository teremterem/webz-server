package org.terems.webz.internals;

import org.terems.webz.WebzDestroyable;
import org.terems.webz.WebzException;

/** TODO !!! describe !!! **/
public interface WebzDestroyableFactory extends WebzDestroyable {

	/** TODO !!! describe !!! **/
	public <T extends WebzDestroyable> T newDestroyable(Class<T> destroyableClass) throws WebzException;

	/** TODO !!! describe !!! **/
	public <T extends WebzDestroyable> T newDestroyable(String destroyableClassName) throws WebzException;

	/** TODO !!! describe !!! **/
	public <T extends WebzDestroyable> T getDestroyableSingleton(Class<T> destroyableClass) throws WebzException;

	/** TODO !!! describe !!! **/
	public <T extends WebzDestroyable> T getDestroyableSingleton(String destroyableClassName) throws WebzException;

}
