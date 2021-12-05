package com.sarang;

import java.awt.*;		//Abstract Window Toolkit
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

//JFrame is responsible for creating a frame
public class DownloadManager extends JFrame implements Observer{
	//Download Link Text field
	private JTextField addTextField;
	
	//Download table's data model
	private DownloadTableModel tableModel;
	
	//Table listing downloads;
	private JTable table;
	
	//Button to manage selected download
	private JButton pauseButton, resumeButton;
	private JButton cancelButton, clearButton;
	
	//currently selected download
	private Download selectedDownload;
	
	//Flag for whether or not table selection is being cleared.
	private boolean clearing;
	
	//Constructor
	public DownloadManager() {
		setTitle("Download Manager made in Core Java");
		
		setSize(800, 400);
		
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				actionExit(); 
			}
		}
		);
		
		//Setting File Menu
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenuItem fileExitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
		fileExitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionExit();
			}
		});
		fileMenu.add(fileExitMenuItem);
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);
		
		//Setting Add Panel
		JPanel addPanel = new JPanel();
		addTextField = new JTextField(40);
		addPanel.add(addTextField);
		JButton addButton = new JButton("Download Now");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionAdd();
			}
		});
		addPanel.add(addButton);
		
		//Setting Downloads table
		tableModel = new DownloadTableModel();
		table = new JTable(tableModel);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				tableSelectionChanged();
			}
		});
		
		//Allow only one row at a time to be selected
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//Setting ProgressBar as renderer for progress column
		ProgressRenderer renderer = new ProgressRenderer(0, 100);
		renderer.setStringPainted(true);	//show progress text
		table.setDefaultRenderer(JProgressBar.class, renderer);
		
		//Setting table's row height large enough to fit JProgressBar
		table.setRowHeight(
				(int) renderer.getPreferredSize().getHeight()
				);
		
		//Setting Downloads Panel
		JPanel downloadsPanel = new JPanel();
		downloadsPanel.setBorder(BorderFactory.createTitledBorder("Downloads"));
		downloadsPanel.setLayout(new BorderLayout());
		downloadsPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		
		//Setting Buttons Panel
		JPanel buttonsPanel = new JPanel();
		pauseButton = new JButton("Pause");
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionPause();
			}
		});
		pauseButton.setEnabled(false);
		buttonsPanel.add(pauseButton);
		
		resumeButton = new JButton("Resume");
		resumeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionResume();
			}
		});
		resumeButton.setEnabled(false);
		buttonsPanel.add(resumeButton);
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionCancel();
			}
		});
		cancelButton.setEnabled(false);
		buttonsPanel.add(cancelButton);
		
		clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionClear();
			}
		});
		clearButton.setEnabled(false);
		buttonsPanel.add(clearButton);
		
		//Add panels to display
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(addPanel, BorderLayout.NORTH);
		getContentPane().add(downloadsPanel, BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
	}
	
	//Exit function
	private void actionExit() {
		System.exit(0);
	}
	
	//Add a new Download, new row
	private void actionAdd() {
        URL verifiedUrl = verifyURL(addTextField.getText());
        if (verifiedUrl != null) {
            tableModel.addDownload(new Download(verifiedUrl));
            addTextField.setText(""); // reset add text field
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid Download URL", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
	
	//Verify download url
	private URL verifyURL(String url) {
		//Only allow HTTP urls
		if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://"))
			return null;
		
		//Verify format of url
		URL verifiedUrl = null;
		try {
			verifiedUrl = new URL(url);
		} catch (Exception e) {
			return null;
		}
		
		//Make sure URL specifies a file
		if (verifiedUrl.getFile().length() < 2)
			return null;
		return verifiedUrl;
	}
	
	//Called when table row selection changes
	private void tableSelectionChanged() {
		//Unregister from receiving notifications from the last selected download
		if (selectedDownload != null)
			selectedDownload.deleteObserver(DownloadManager.this);
		//If not in the middle of clearing a download, set the selected download and register to receive notifications from it.
		if (!clearing) {
			selectedDownload = tableModel.getDownload(table.getSelectedRow());
			selectedDownload.addObserver(DownloadManager.this);
			updateButtons();
		}
	}
	
	//Pause Selected download
	private void actionPause() {
		selectedDownload.pause();
		updateButtons();
	}
	
	//Resume selected download
	private void actionResume() {
		selectedDownload.resume();
		updateButtons();
	}
	
	//cancel selected download
	private void actionCancel() {
		selectedDownload.cancel();
		updateButtons();
	}
	
	//clear selected download
	private void actionClear() {
		clearing = true;
		tableModel.clearDownload(table.getSelectedRow());
		clearing = false;
		selectedDownload = null;
		updateButtons();
	}
	
	//Update each button's status based off of the currently selected download's status
	private void updateButtons() {
		if (selectedDownload != null) {
			int status = selectedDownload.getStatus();
			switch(status) {
			case Download.DOWNLOADING:
				pauseButton.setEnabled(true);
				resumeButton.setEnabled(false);
				cancelButton.setEnabled(true);
				clearButton.setEnabled(false);
				break;
			case Download.PAUSED:
				pauseButton.setEnabled(false);
				resumeButton.setEnabled(true);
				cancelButton.setEnabled(true);
				clearButton.setEnabled(false);
				break;
			case Download.ERROR:
				pauseButton.setEnabled(false);
				resumeButton.setEnabled(true);
				cancelButton.setEnabled(false);
				clearButton.setEnabled(true);
				break;
			default:	//Complete or Cancelled
				pauseButton.setEnabled(false);
				resumeButton.setEnabled(false);
				cancelButton.setEnabled(false);
				clearButton.setEnabled(true);
			}
		}
		else {
			//No download is selected in table
			pauseButton.setEnabled(false);
			resumeButton.setEnabled(false);
			cancelButton.setEnabled(false);
			clearButton.setEnabled(false);
		}
	}
	
	//Update is called when a Download notifies its observers of any changes.
	public void update(Observable o, Object arg) {
		//Update buttons if the selected download has changed
		if (selectedDownload != null && selectedDownload.equals(o))
			updateButtons();
	}
	
	//Run Download Manager
	public static void main(String[] args) {
		DownloadManager manager = new DownloadManager();
		ImageIcon img = new ImageIcon("icon.png");
		manager.setIconImage(img.getImage());
		manager.show();
	}
}
















