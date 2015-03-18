package org.terems.webz.filter.helpers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.pegdown.LinkRenderer;
import org.pegdown.ast.AnchorLinkNode;
import org.pegdown.ast.AutoLinkNode;
import org.pegdown.ast.ExpImageNode;
import org.pegdown.ast.ExpLinkNode;
import org.pegdown.ast.MailLinkNode;
import org.pegdown.ast.RefImageNode;
import org.pegdown.ast.RefLinkNode;
import org.pegdown.ast.WikiLinkNode;
import org.terems.webz.WebzConfig;

public class ConfigurableLinkRenderer extends LinkRenderer {

	private boolean nofollowForRelativeUris = false;
	private String targetForRelativeUris = null;

	private boolean nofollowForThisDomain = false;
	private String targetForThisDomain = null;

	private boolean nofollowForAppDomains = false;
	private String targetForAppDomains = null;

	private Set<String> appDomains = new HashSet<String>(Arrays.asList("www.terems.org", "ww.webz.bz"));

	private boolean nofollowForOtherDomains = true;
	private String targetForOtherDomains = "_blank";

	public ConfigurableLinkRenderer(WebzConfig config) {

		// TODO support configurable rel="nofollow" for links to foreign domains
		// TODO support configurable target="xxx"
	}

	private Rendering nofollow(Rendering rendering) {
		return rendering.withAttribute(new Attribute("rel", "nofollow"));
	}

	private Rendering target(Rendering rendering, String target) {
		return rendering.withAttribute(new Attribute("target", target));
	}

	private Rendering whatAboutAttributes(Rendering rendering) {

		if (nofollowForRelativeUris) {
			rendering = nofollow(rendering);
		}
		if (targetForOtherDomains != null) {
			rendering = target(rendering, targetForOtherDomains);
		}
		// TODO implement all link attribute options

		return rendering;
	}

	@Override
	public Rendering render(AnchorLinkNode node) {

		return whatAboutAttributes(super.render(node));
	}

	@Override
	public Rendering render(AutoLinkNode node) {

		return whatAboutAttributes(super.render(node));
	}

	@Override
	public Rendering render(ExpLinkNode node, String text) {

		return whatAboutAttributes(super.render(node, text));
	}

	@Override
	public Rendering render(ExpImageNode node, String text) {

		return whatAboutAttributes(super.render(node, text));
	}

	@Override
	public Rendering render(MailLinkNode node) {

		return whatAboutAttributes(super.render(node));
	}

	@Override
	public Rendering render(RefLinkNode node, String url, String title, String text) {

		return whatAboutAttributes(super.render(node, url, title, text));
	}

	@Override
	public Rendering render(RefImageNode node, String url, String title, String alt) {

		return whatAboutAttributes(super.render(node, url, title, alt));
	}

	@Override
	public Rendering render(WikiLinkNode node) {

		return whatAboutAttributes(super.render(node));
	}

}
