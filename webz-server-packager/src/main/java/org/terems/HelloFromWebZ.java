package org.terems;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.tomcat.util.http.fileupload.IOUtils;

public class HelloFromWebZ {

	private static final String FAVICON_RESOURCE_PREFIX = "/favicon/favicon";
	private static final String[] FAVICON_RESOURCE_SUFFIXES = { "16.png", "32.png", "64.png", "128.png", "256.png", "512.png", "1024.png" };

	private static void createAndShowMainWindow() {

		JFrame frame = new JFrame("WebZ Server v0.9 beta (Pedesis)");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		List<Image> favicons = fetchFavicons();
		if (!favicons.isEmpty()) {
			frame.setIconImages(favicons);
		}

		JPanel outerPanel = new JPanel();
		outerPanel.setLayout(new GridBagLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		outerPanel.add(panel, new GridBagConstraints());

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(outerPanel, BorderLayout.CENTER);

		JLabel startedLabel = new JLabel("Server started.");
		startedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		startedLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
		panel.add(startedLabel);

		JLabel portLabel = new JLabel("HTTP port: 8887"); // TODO display the real port number
		portLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(portLabel);

		JLabel shutdownLabel = new JLabel("Close this window to shutdown...");
		shutdownLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(shutdownLabel);

		JLabel licenseLabel = new JLabel("This software is licensed under GNU Affero General Public License v3.0");
		licenseLabel.setHorizontalAlignment(SwingConstants.CENTER);
		licenseLabel.setFont(new Font(Font.DIALOG, Font.ITALIC, 14));
		frame.getContentPane().add(licenseLabel, BorderLayout.SOUTH);

		frame.setResizable(false);
		frame.setMinimumSize(new Dimension(560, 180));
		frame.pack();

		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);

		frame.setVisible(true);
	}

	private static List<Image> fetchFavicons() {

		List<Image> favicons = new ArrayList<Image>(FAVICON_RESOURCE_SUFFIXES.length);

		ByteArrayOutputStream faviconOut = new ByteArrayOutputStream();
		for (String faviconSuffix : FAVICON_RESOURCE_SUFFIXES) {
			try {
				faviconOut.reset();

				InputStream resourceIn = HelloFromWebZ.class.getResourceAsStream(FAVICON_RESOURCE_PREFIX + faviconSuffix);
				if (resourceIn != null) {

					IOUtils.copy(resourceIn, faviconOut);
					favicons.add(new ImageIcon(faviconOut.toByteArray()).getImage());
				}
			} catch (IOException e) {
				// it's just a favicon - ignore
			}
		}
		return favicons;
	}

	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowMainWindow();
			}
		});
	}

}
