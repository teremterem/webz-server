/*
 * WebZ Server can serve web pages from various local and remote file sources.
 * Copyright (C) 2014-2015  Oleksandr Tereschenko <http://www.terems.org/>
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

package org.terems.webz.impl;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzException;
import org.terems.webz.WebzInputStreamDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.WebzMetadataProxy;
import org.terems.webz.internals.ParentChildrenMetadata;
import org.terems.webz.internals.WebzFileSystemImpl;
import org.terems.webz.internals.base.BaseWebzFileSystemImpl;

// TODO abstract away stuff common for SimpleFileSystemOverlayImpl and SiteAndSpaFileSystemImpl
public class SimpleFileSystemOverlayImpl extends BaseWebzFileSystemImpl {

	private static final Logger LOG = LoggerFactory.getLogger(SimpleFileSystemOverlayImpl.class);

	private WebzFileSystemImpl primaryFsImpl;
	private WebzFileSystemImpl secondaryFsImpl;

	private String primaryOrigin;
	private String secondaryOrigin;
	private String[] bothOrigins;
	private String[] primaryOriginArray;
	private String[] secondaryOriginArray;

	private String uniqueId;

	public SimpleFileSystemOverlayImpl(WebzFileSystemImpl primaryFsImpl, String primaryOrigin, WebzFileSystemImpl secondaryFsImpl,
			String secondaryOrigin) {

		this.primaryFsImpl = primaryFsImpl;
		this.primaryOrigin = primaryOrigin;
		this.secondaryFsImpl = secondaryFsImpl;
		this.secondaryOrigin = secondaryOrigin;
		this.bothOrigins = new String[] { primaryOrigin, secondaryOrigin };
		this.primaryOriginArray = new String[] { primaryOrigin };
		this.secondaryOriginArray = new String[] { secondaryOrigin };
	}

	@Override
	protected void init() {

		uniqueId = primaryFsImpl.getUniqueId() + "-" + secondaryFsImpl.getUniqueId();

		if (LOG.isInfoEnabled()) {
			LOG.info("'" + uniqueId + "' hybrid file system was created");
		}
	}

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	private static class FileFound {

		WebzMetadata proxiedMetadata;
		WebzFileSystemImpl primaryFsImpl;
		WebzFileSystemImpl secondaryFsImpl; // can be null
		String primaryOrigin;
		String secondaryOrigin; // can be null
		String actualPathname;

		FileFound(WebzMetadata metadataToProxy, String[] origins, WebzFileSystemImpl primaryFsImpl, WebzFileSystemImpl secondaryFsImpl,
				String primaryOrigin, String secondaryOrigin, String actualPathname) {

			this.proxiedMetadata = new ProxiedMetadata(metadataToProxy, origins);
			this.primaryFsImpl = primaryFsImpl;
			this.secondaryFsImpl = secondaryFsImpl;
			this.primaryOrigin = primaryOrigin;
			this.secondaryOrigin = secondaryOrigin;
			this.actualPathname = actualPathname;
		}

	}

	private static class ProxiedMetadata extends WebzMetadataProxy {

		WebzMetadata metadataToProxy;
		String[] originNames;

		ProxiedMetadata(WebzMetadata metadataToProxy, String[] originNames) {
			this.metadataToProxy = metadataToProxy;
			this.originNames = originNames;
		}

		@Override
		protected WebzMetadata getInternalMetadata() {
			return metadataToProxy;
		}

		@Override
		public String[] getOrigins() {
			return originNames;
		}

	}

	private FileFound findFile(String pathname) throws IOException, WebzException {

		return hitAgainstFullPathname(pathname);
		// TODO get rid of hitAgainstFullPathname() method when caching is implemented (integrate it's logic into the algorithm below)
	}

	private FileFound hitAgainstFullPathname(String pathname) throws IOException, WebzException {

		WebzMetadata metadata = primaryFsImpl.getMetadata(pathname);
		if (metadata != null) {

			if (metadata.isFolder()) {

				WebzMetadata secondaryMetadata = secondaryFsImpl.getMetadata(pathname);
				if (secondaryMetadata != null && secondaryMetadata.isFolder()) {
					return new FileFound(metadata, bothOrigins, primaryFsImpl, secondaryFsImpl, primaryOrigin, secondaryOrigin, pathname);
				}
			}
			return new FileFound(metadata, primaryOriginArray, primaryFsImpl, null, primaryOrigin, null, pathname);
		}

		metadata = secondaryFsImpl.getMetadata(pathname);
		if (metadata != null) {

			return new FileFound(metadata, secondaryOriginArray, primaryFsImpl, null, secondaryOrigin, null, pathname);
		}

		return null;
	}

	@Override
	public WebzMetadata getMetadata(String pathname) throws IOException, WebzException {

		FileFound found = findFile(pathname);
		if (found == null) {
			return null;
		}
		return found.proxiedMetadata;
	}

	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathname) throws IOException, WebzException {

		FileFound found = findFile(parentPathname);
		if (found == null) {
			return null;
		}
		return new ParentChildrenMetadata(found.proxiedMetadata, fetchMergedChildren(found), null);
	}

	@Override
	public Map<String, WebzMetadata> getChildPathnamesAndMetadata(String parentPathname) throws IOException, WebzException {

		FileFound found = findFile(parentPathname);
		if (found == null) {
			return null;
		}
		return fetchMergedChildren(found);
	}

	private Map<String, WebzMetadata> fetchMergedChildren(FileFound found) throws IOException, WebzException {

		Map<String, WebzMetadata> primary = found.primaryFsImpl.getChildPathnamesAndMetadata(found.actualPathname);
		Map<String, WebzMetadata> secondary = null;
		if (found.secondaryFsImpl != null) {
			secondary = found.secondaryFsImpl.getChildPathnamesAndMetadata(found.actualPathname);
		}
		String[] originsPrimary = new String[] { found.primaryOrigin };
		String[] originsSecondary = new String[] { found.secondaryOrigin };
		String[] originsBoth = new String[] { found.secondaryOrigin, found.primaryOrigin };

		if (secondary == null) {
			return wrapMetadataMap(primary, originsPrimary);
		}
		if (primary == null) {
			return wrapMetadataMap(secondary, originsSecondary);
		}

		secondary = new LinkedHashMap<String, WebzMetadata>(secondary);

		Map<String, WebzMetadata> merged = new LinkedHashMap<String, WebzMetadata>(primary);
		for (Map.Entry<String, WebzMetadata> mergedEntry : merged.entrySet()) {

			WebzMetadata fromMerged = mergedEntry.getValue();
			WebzMetadata alsoInSecondary = secondary.remove(mergedEntry.getKey());
			if (alsoInSecondary == null) {

				mergedEntry.setValue(new ProxiedMetadata(fromMerged, originsPrimary));
			} else {

				if (fromMerged.isFolder() && alsoInSecondary.isFolder()) {
					mergedEntry.setValue(new ProxiedMetadata(fromMerged, originsBoth));
				} else {
					mergedEntry.setValue(new ProxiedMetadata(fromMerged, originsPrimary));
				}
			}
		}
		for (Map.Entry<String, WebzMetadata> secondaryNewEntry : secondary.entrySet()) {
			merged.put(secondaryNewEntry.getKey(), new ProxiedMetadata(secondary.get(secondaryNewEntry.getValue()), originsSecondary));
		}

		return merged;
	}

	private Map<String, WebzMetadata> wrapMetadataMap(Map<String, WebzMetadata> metadataMap, String[] origins) {

		if (metadataMap != null) {

			for (Map.Entry<String, WebzMetadata> entry : metadataMap.entrySet()) {
				entry.setValue(new ProxiedMetadata(entry.getValue(), origins));
			}
		}
		return metadataMap;
	}

	@Override
	public WebzInputStreamDownloader getFileDownloader(String pathname) throws IOException, WebzException {

		FileFound found = findFile(pathname);
		if (found == null) {
			return null;
		}

		return found.primaryFsImpl.getFileDownloader(found.actualPathname);
	}

}
