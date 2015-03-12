package org.terems.webz.filter.helpers;

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

	public ConfigurableLinkRenderer(WebzConfig config) {

		// TODO support configurable rel="nofollow" for links to foreign domains
		// TODO support configurable target="xxx"
	}

	@Override
	public Rendering render(AnchorLinkNode node) {
		return super.render(node);
	}

	@Override
	public Rendering render(AutoLinkNode node) {
		return super.render(node);
	}

	@Override
	public Rendering render(ExpLinkNode node, String text) {
		return super.render(node, text);
	}

	@Override
	public Rendering render(ExpImageNode node, String text) {
		return super.render(node, text);
	}

	@Override
	public Rendering render(MailLinkNode node) {
		return super.render(node);
	}

	@Override
	public Rendering render(RefLinkNode node, String url, String title, String text) {
		return super.render(node, url, title, text);
	}

	@Override
	public Rendering render(RefImageNode node, String url, String title, String alt) {
		return super.render(node, url, title, alt);
	}

	@Override
	public Rendering render(WikiLinkNode node) {
		return super.render(node);
	}

}
