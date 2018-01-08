package com.mathhead200.prnge;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;


public class PrngeGUI
{
	@SuppressWarnings("serial")
	public static class GBC extends GridBagConstraints {
		public GBC() {
			anchor = GridBagConstraints.CENTER;
			insets = new Insets(4, 4, 4, 4);
		}
	}

	@SuppressWarnings("serial")
	public static class FileStuff extends Panel implements ActionListener, InputMethodListener {
		private JFrame frame = new JFrame();
		private JFileChooser chooser = new JFileChooser();
		private JLabel label = new JLabel();
		private JTextField textField = new JTextField();
		private JButton button = new JButton();

		public FileStuff(String text) {
			super( new GridBagLayout() );
			frame.setVisible(false);
			frame.setTitle(text);
			frame.setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
			frame.getContentPane().add(chooser);
			frame.setSize( new Dimension(600, 450) );
			frame.setLocation( new Point(50, 50) );
			frame.validate();
			chooser.addActionListener(this);
			label.setText(text + ":");
			textField.setColumns(50);
			textField.addInputMethodListener(this);
			button.setText("Browse");
			button.addActionListener(this);
			GridBagConstraints c = new GBC();
			add( label, c );
			c = new GBC();
			c.gridx = 1;
			c.weightx = 4;
			add( textField, c );
			c = new GBC();
			c.gridx = 2;
			add( button, c );
		}

		public void actionPerformed(ActionEvent e) {
			if( e.getSource() == button ) {
				frame.setVisible(true);
			} else if( e.getSource() == chooser ) {
				if( e.getActionCommand().equals("ApproveSelection") ) {
					textField.setText( chooser.getSelectedFile().getAbsolutePath() );
					frame.setVisible(false);
				} else if( e.getActionCommand().equals("CancelSelection") ) {
					frame.setVisible(false);
				}
			}
		}

		public void inputMethodTextChanged(InputMethodEvent event) {
			File file = getFile();
			if( !file.exists() ) {
				label.setToolTipText("Not a current file");
			} else if( file.isFile() ) {
				label.setToolTipText("File found");
			} else if( file.isDirectory() ) {
				label.setToolTipText("Directory found");
			}
		}

		public void caretPositionChanged(InputMethodEvent event) {}

		public File getFile() {
			return new File( textField.getText() );
		}
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final JFrame frame = new JFrame("PRNGE");
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setContentPane( new JPanel(new GridBagLayout()) );

		final FileStuff inStuff = new FileStuff("Input File");
		GridBagConstraints c = new GBC();
		c.gridwidth = 4;
		frame.getContentPane().add( inStuff, c );

		final FileStuff outStuff = new FileStuff("Output File");
		c = new GBC();
		c.gridy = 1;
		c.gridwidth = 4;
		frame.getContentPane().add( outStuff, c );

		c = new GBC();
		c.gridy = 2;
		c.gridwidth = 2;
		frame.getContentPane().add( new JLabel("Key:"), c );

		final JPasswordField pwField = new JPasswordField(16);
		c = new GBC();
		c.gridy = 2;
		c.gridx = 2;
		c.gridwidth = 2;
		frame.getContentPane().add( pwField, c );

		c = new GBC();
		c.gridy = 3;
		frame.getContentPane().add( new JLabel("Bloat Factor:"), c );

		final JTextField bloatField = new JTextField("1.3", 8);
		c = new GBC();
		c.gridy = 3;
		c.gridx = 1;
		frame.getContentPane().add( bloatField, c );

		c = new GBC();
		c.gridy = 3;
		c.gridx = 2;
		frame.getContentPane().add( new JLabel("Chunk Size:"), c );

		final JTextField chunkField = new JTextField("1", 8);
		c = new GBC();
		c.gridy = 3;
		c.gridx = 3;
		frame.getContentPane().add( chunkField, c );

		final JButton encButton = new JButton("Encrypt");
		c = new GBC();
		c.gridy = 4;
		c.gridwidth = 2;
		frame.getContentPane().add( encButton, c );

		final JButton decButton = new JButton("Decrypt");
		c = new GBC();
		c.gridy = 4;
		c.gridx = 2;
		c.gridwidth = 2;
		frame.getContentPane().add( decButton, c );

		frame.pack();
		{
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Dimension scrDim = toolkit.getScreenSize();
			frame.setLocation( new Point(
				(scrDim.width - frame.getWidth()) / 2,
				(scrDim.height - frame.getHeight()) / 4
			));
		}
		frame.validate();
		frame.setVisible(true);

		encButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				encButton.setEnabled(false);
				decButton.setEnabled(false);
				try {
					Prnge prnge = new Prnge(
						new String( pwField.getPassword() ),
						Double.parseDouble( bloatField.getText() ),
						Integer.parseInt( chunkField.getText() )
					);
					FileInputStream fin = new FileInputStream( inStuff.getFile() );
					FileOutputStream fout = new FileOutputStream( outStuff.getFile() );
					prnge.encrypt( fin, fout );
					fin.close();
					fout.close();
					JOptionPane.showMessageDialog( frame,
							"[" + inStuff.getFile().getName() + "] has been encrypted to ["
								+ outStuff.getFile().getName() + "]",
							"Encryption Done", JOptionPane.INFORMATION_MESSAGE );
				} catch(NumberFormatException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog( frame, "Encryption failed:\n" + ex,
							"Encryption Failed", JOptionPane.ERROR_MESSAGE );
				} catch(IOException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog( frame, "Encryption failed:\n" + ex,
							"Encryption Failed", JOptionPane.ERROR_MESSAGE );
				}
				encButton.setEnabled(true);
				decButton.setEnabled(true);
			}
		});
		decButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				encButton.setEnabled(true);
				decButton.setEnabled(true);
				try {
					Prnge prnge = new Prnge(
						new String( pwField.getPassword() ),
						Double.parseDouble( bloatField.getText() ),
						Integer.parseInt( chunkField.getText() )
					);
					FileInputStream fin = new FileInputStream( inStuff.getFile() );
					FileOutputStream fout = new FileOutputStream( outStuff.getFile() );
					prnge.decrypt( fin, fout );
					fin.close();
					fout.close();
					JOptionPane.showMessageDialog( frame,
							"[" + inStuff.getFile().getName() + "] has been decrypted to ["
								+ outStuff.getFile().getName() + "]",
							"Decryption Done", JOptionPane.INFORMATION_MESSAGE );
				} catch(NumberFormatException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog( frame, "Encryption failed:\n" + ex,
							"Encryption Failed", JOptionPane.ERROR_MESSAGE );
				} catch(IOException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog( frame, "Encryption failed:\n" + ex,
							"Encryption Failed", JOptionPane.ERROR_MESSAGE );
				}
				encButton.setEnabled(true);
				decButton.setEnabled(true);
			}
		});
	}
}
