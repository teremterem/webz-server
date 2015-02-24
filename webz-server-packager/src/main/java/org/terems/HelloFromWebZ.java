package org.terems;

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
import javax.swing.SwingUtilities;

import org.apache.tomcat.util.http.fileupload.IOUtils;

public class HelloFromWebZ {

	private static final String[] ICON_RESOURCE_NAMES = { "/favicon16.png", "/favicon32.png", "/favicon64.png", "/favicon128.png",
			"/favicon256.png", "/favicon512.png", "/favicon1024.png" };

	private static void createAndShowMainWindow() {

		JFrame frame = new JFrame("WebZ Server v0.9 beta (Pedesis)");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		List<Image> favicons = fetchFavicons();
		if (!favicons.isEmpty()) {
			frame.setIconImages(favicons);
		}

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		frame.getContentPane().setLayout(new GridBagLayout());
		frame.getContentPane().add(panel, new GridBagConstraints());

		JLabel startedLabel = new JLabel("Server started.");
		startedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		startedLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
		panel.add(startedLabel);

		JLabel portLabel = new JLabel("HTTP port: 8887");
		portLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(portLabel);

		JLabel shutdownLabel = new JLabel("Close this window to shutdown...");
		shutdownLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(shutdownLabel);

		frame.setResizable(false);
		frame.setSize(new Dimension(600, 200));

		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);

		frame.setVisible(true);
	}

	private static List<Image> fetchFavicons() {

		List<Image> favicons = new ArrayList<Image>(ICON_RESOURCE_NAMES.length);

		ByteArrayOutputStream faviconOut = new ByteArrayOutputStream();
		for (String iconResourceName : ICON_RESOURCE_NAMES) {
			try {
				faviconOut.reset();

				InputStream resourceIn = HelloFromWebZ.class.getResourceAsStream(iconResourceName);
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
