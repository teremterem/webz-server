package org.terems;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.tomcat.util.http.fileupload.IOUtils;

public class HelloFromWebZ {

	private static void createAndShowMainWindow() {

		JFrame frame = new JFrame("WebZ Server v0.9 (Pedesis)");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		ByteArrayOutputStream faviconStream = new ByteArrayOutputStream();
		try {
			IOUtils.copy(HelloFromWebZ.class.getResourceAsStream("/favicon.png"), faviconStream);

			ImageIcon img = new ImageIcon(faviconStream.toByteArray());
			frame.setIconImage(img.getImage());
		} catch (IOException e) {
			// it's just a favicon - ignore
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
		// frame.pack();
		frame.setSize(new Dimension(600, 200));

		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);

		frame.setVisible(true);
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
