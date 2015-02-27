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

package org.terems;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.tomcat.util.http.fileupload.IOUtils;

public class WebzLauncherGUI {

	private static final String FAVICON_RESOURCE_PREFIX = "/favicon/favicon";
	private static final String[] FAVICON_RESOURCE_SUFFIXES = { "16.png", "32.png", "64.png", "128.png", "256.png", "512.png", "1024.png" };

	private static final String STARTING_MSG = "Starting...";
	private static final String SERVER_STARTED_MSG = "Server started.";
	private static final String HTTP_PORT_MSG_PREFIX = "HTTP port: ";
	private static final String CLOSE_TO_SHUTDOWN_MSG = "Close this window to shutdown...";

	private static JFrame frame;
	private static JLabel startLabel;
	private static JLabel portLabel;
	private static JLabel shutdownLabel;

	public static void initGuiSafe(final String title) {

		try {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					createAndShowMainWindow(title);
				}
			});
		} catch (Throwable th) {
			th.printStackTrace();
			// GUI dispatching is not important - in case of failure just print the stack trace and proceed
		}
	}

	public static void showServerStartedSafe(final int httpPortNumber) {

		try {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {

					if (startLabel != null) {
						startLabel.setText(SERVER_STARTED_MSG);
					}
					if (portLabel != null) {
						portLabel.setText(HTTP_PORT_MSG_PREFIX + httpPortNumber);
					}
					if (shutdownLabel != null) {
						shutdownLabel.setText(CLOSE_TO_SHUTDOWN_MSG);
					}
					if (frame != null) {
						frame.pack();
					}
				}
			});
		} catch (Throwable th) {
			th.printStackTrace();
			// GUI dispatching is not important - in case of failure just print the stack trace and proceed
		}
	}

	public static void showFatalAndExit(final int exitCode, final String errorMessage) {

		try {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {

					try {
						JOptionPane.showMessageDialog(frame, errorMessage, "Fatal Error", JOptionPane.ERROR_MESSAGE);
					} catch (Throwable th) {
						th.printStackTrace();
					}
					System.exit(exitCode);
				}
			});
		} catch (Throwable th) {
			th.printStackTrace();
			// GUI dispatching is not important - in case of failure just print the stack trace and exit
			System.exit(exitCode);
		}
	}

	private static void createAndShowMainWindow(String title) {

		frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		List<Image> favicons = fetchFavicons();
		if (!favicons.isEmpty()) {
			frame.setIconImages(favicons);
		}

		JPanel outerPanel = new JPanel();
		outerPanel.setLayout(new GridBagLayout());

		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
		outerPanel.add(innerPanel, new GridBagConstraints());

		Container frameContentPane = frame.getContentPane();

		frameContentPane.setLayout(new BorderLayout());
		frameContentPane.add(outerPanel, BorderLayout.CENTER);

		JLabel licenseLabel = new JLabel("This software is released under the terms of GNU Affero General Public License v3.0");
		licenseLabel.setHorizontalAlignment(SwingConstants.CENTER);
		licenseLabel.setFont(new Font(Font.DIALOG, Font.ITALIC, 13));
		frameContentPane.add(licenseLabel, BorderLayout.SOUTH);

		startLabel = new JLabel(STARTING_MSG);
		startLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		startLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
		innerPanel.add(startLabel);

		portLabel = new JLabel();
		portLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		innerPanel.add(portLabel);

		shutdownLabel = new JLabel();
		shutdownLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		innerPanel.add(shutdownLabel);

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

				InputStream resourceIn = WebzLauncherGUI.class.getResourceAsStream(FAVICON_RESOURCE_PREFIX + faviconSuffix);
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

}
