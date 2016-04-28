/*
 * Copyright (C) 2007 - 2010 Gabriel Zanetti
 */
package controller;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import controller.extras.LookAndFeelInfoWrapper;
import controller.extras.TimeUnitConverter;
import controller.options.AudioConfigurationController;
import controller.options.CommandConfigurationController;
import controller.options.MailConfigurationController;
import controller.options.VisualConfigurationController;
import model.configuration.ConfigurationManager;
import model.configuration.IPMonitorProperties;
import model.configuration.IPMonitorPropertiesManager;
import model.ipmonitor.IPMonitor;
import model.ipmonitor.exceptions.InvalidIntervalException;
import model.logger.MainLogger;
import model.logger.exceptions.InvalidMaxDaysToKeepLogs;
import model.notification.AudioNotification;
import model.notification.CommandNotification;
import model.notification.MailNotification;
import model.notification.VisualNotification;
import model.notification.configuration.AudioConfiguration;
import model.notification.configuration.CommandConfiguration;
import model.notification.performers.AudioPerformer;
import model.notification.performers.CommandPerformer;
import model.notification.performers.MailPerformer;
import model.notification.performers.VisualPerformer;
import model.service.ServiceManager;
import model.service.helpers.ProcessResult;
import view.OptionsView;

public class OptionsController {

	private IPMonitor ipMonitor;
	private OptionsView optionsView;
	private String serviceName;

	public OptionsController(JFrame owner, IPMonitor ipMonitor) {
		this.ipMonitor = ipMonitor;
		optionsView = new OptionsView(owner, ipMonitor);
		optionsView.getJButtonOk().addActionListener(new JButtonOkAction());
		optionsView.getJButtonCancel().addActionListener(new JButtonCancelAction());
		optionsView.getJButtonApply().addActionListener(new JButtonApplyAction());

		optionsView.getJPanelOptionsNotification().getJButtonAudioConfiguration()
				.addActionListener(new JButtonAudioConfiguration());
		optionsView.getJPanelOptionsNotification().getJButtonAudioTest().addActionListener(new JButtonAudioTest());
		optionsView.getJPanelOptionsNotification().getJButtonMailConfiguration()
				.addActionListener(new JButtonMailConfiguration());
		optionsView.getJPanelOptionsNotification().getJButtonMailTest().addActionListener(new JButtonMailTest());
		optionsView.getJPanelOptionsNotification().getJButtonVisualConfiguration()
				.addActionListener(new JButtonVisualConfiguration());
		optionsView.getJPanelOptionsNotification().getJButtonVisualTest().addActionListener(new JButtonVisualTest());
		optionsView.getJPanelOptionsNotification().getJButtonCommandConfiguration()
				.addActionListener(new JButtonCommandConfiguration());
		optionsView.getJPanelOptionsNotification().getJButtonCommandTest().addActionListener(new JButtonCommandTest());

		serviceName = ServiceManager.getInstance().getService().getServiceName().toLowerCase();
		optionsView.getJPanelOptionsService().getJButtonInstall().addActionListener(new JButtonInstallServiceAction());
		optionsView.getJPanelOptionsService().getJButtonUninstall()
				.addActionListener(new JButtonUninstallServiceAction());
		optionsView.getJPanelOptionsService().getJButtonStart().addActionListener(new JButtonStartServiceAction());
		optionsView.getJPanelOptionsService().getJButtonStop().addActionListener(new JButtonStopServiceAction());
		optionsView.getJPanelOptionsService().getJButtonTest().addActionListener(new JButtonTestServiceAction());

		optionsView.getJPanelOptionsLogging().getJCheckBoxEnableLogging()
				.addActionListener(new JCheckBoxEnableLoggingAction());
		optionsView.getRootPane().registerKeyboardAction(optionsView.getJButtonCancel().getActionListeners()[0],
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		setEnableLogging();
		optionsView.setVisible(true);
	}

	private void setInterval() throws NumberFormatException, InvalidIntervalException {
		int hours, minutes, seconds;
		try {
			hours = Integer.valueOf(optionsView.getJPanelOptionsMonitor().getJTextFieldHours().getText());
			minutes = Integer.valueOf(optionsView.getJPanelOptionsMonitor().getJTextFieldMinutes().getText());
			seconds = Integer.valueOf(optionsView.getJPanelOptionsMonitor().getJTextFieldSeconds().getText());
			if ((hours < 0) || (minutes < 0 || minutes > 59) || (seconds < 0 || seconds > 59)) {
				throw new NumberFormatException();
			}
			ipMonitor.setInterval(hours * TimeUnitConverter.HOURS + minutes * TimeUnitConverter.MINUTES + seconds);
		} catch (NumberFormatException e1) {
			JOptionPane.showMessageDialog(null, "Interval is not correct. Please enter a valid interval.", "Error",
					JOptionPane.ERROR_MESSAGE);
			optionsView.getJPanelOptionsMonitor().getJTextFieldHours().requestFocus();
			throw e1;
		} catch (InvalidIntervalException e2) {
			JOptionPane.showMessageDialog(null, "Interval is not correct. It can not be less than 10 minutes.", "Error",
					JOptionPane.ERROR_MESSAGE);
			throw e2;
		}
	}

	private void setAutoStart() {
		ConfigurationManager.getInstance()
				.setAutostart(optionsView.getJPanelOptionsMonitor().getJCheckBoxAutoStart().isSelected());
	}

	private void setUrl() throws MalformedURLException {
		try {
			ipMonitor.setUrl(optionsView.getJPanelOptionsMonitor().getJTextFieldURL().getText());
		} catch (MalformedURLException e) {
			JOptionPane.showMessageDialog(null,
					"The URL is not correct. Use a sintax similar to:\nhttp://www.server.com", "Error",
					JOptionPane.ERROR_MESSAGE);
			optionsView.getJPanelOptionsMonitor().getJTextFieldURL().requestFocus();
			throw e;
		}
	}

	private void setAudioNotification() {
		if (optionsView.getJPanelOptionsNotification().getJCheckBoxEnableAudioNotification().isSelected()) {
			ipMonitor.addIPMonitorListener(AudioNotification.getInstance());
		} else {
			ipMonitor.removeIPMonitorListener(AudioNotification.getInstance());
		}
	}

	private void setMailNotification() {
		if (optionsView.getJPanelOptionsNotification().getJCheckBoxEnableMailNotification().isSelected()) {
			ipMonitor.addIPMonitorListener(MailNotification.getInstance());
		} else {
			ipMonitor.removeIPMonitorListener(MailNotification.getInstance());
		}
	}

	private void setVisualNotification() {
		if (optionsView.getJPanelOptionsNotification().getJCheckBoxEnableVisualNotification().isSelected()) {
			ipMonitor.addIPMonitorListener(VisualNotification.getInstance());
		} else {
			ipMonitor.removeIPMonitorListener(VisualNotification.getInstance());
		}
	}

	private void setCommandNotification() {
		if (optionsView.getJPanelOptionsNotification().getJCheckBoxEnableCommandNotification().isSelected()) {
			ipMonitor.addIPMonitorListener(CommandNotification.getInstance());
		} else {
			ipMonitor.removeIPMonitorListener(CommandNotification.getInstance());
		}
	}

	private void setLookAndFeel() {
		try {
			LookAndFeelInfoWrapper lookAndFeelInfoWrapper = ((LookAndFeelInfoWrapper) optionsView
					.getJPanelOptionsInterface().getJListLookAndFeel().getSelectedValue());
			if (UIManager.getLookAndFeel().getName().equals(lookAndFeelInfoWrapper.getName())) {
				return;
			}
			try {
				ConfigurationManager.getInstance().getVisualConfigurationManager()
						.setLookAndFeelClassName(lookAndFeelInfoWrapper.getClassName());
				UIManager.setLookAndFeel(
						ConfigurationManager.getInstance().getVisualConfigurationManager().getLookAndFeelClassName());
				SwingUtilities.updateComponentTreeUI(optionsView.getOwner());
				SwingUtilities.updateComponentTreeUI(optionsView);
				optionsView.pack();
				optionsView.getOwner().pack();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	private void setEnableLogging() {
		boolean enabled = optionsView.getJPanelOptionsLogging().getJCheckBoxEnableLogging().isSelected();
		JPanel loggingConfigurationPanel = optionsView.getJPanelOptionsLogging().getJPanelLoggingConfiguration();
		Component[] components = loggingConfigurationPanel.getComponents();
		loggingConfigurationPanel.setEnabled(enabled);
		for (Component component : components) {
			component.setEnabled(enabled);
		}
	}

	private void setLogging() {
		MainLogger.getInstance()
				.setEnabled(optionsView.getJPanelOptionsLogging().getJCheckBoxEnableLogging().isSelected());
	}

	private void setMaxDaysToKeepLogs() throws NumberFormatException, InvalidMaxDaysToKeepLogs {
		int days;
		try {
			days = Integer.valueOf(optionsView.getJPanelOptionsLogging().getJTextFieldDaysToKeepLogs().getText());
			MainLogger.getInstance().setMaxDaysToKeepLogs(days);
		} catch (NumberFormatException e1) {
			JOptionPane.showMessageDialog(null,
					"The number of days must be a positive integer. Please enter a valid number of days.", "Error",
					JOptionPane.ERROR_MESSAGE);
			optionsView.getJPanelOptionsLogging().getJTextFieldDaysToKeepLogs().requestFocus();
			throw e1;
		} catch (InvalidMaxDaysToKeepLogs e2) {
			if (MainLogger.getInstance().isEnabled()) {
				JOptionPane.showMessageDialog(null,
						"The number of days must be a positive integer. Please enter a valid number of days.\n"
								+ "If you don't want to keep any log you should disable logging.",
						"Error", JOptionPane.ERROR_MESSAGE);
				optionsView.getJPanelOptionsLogging().getJTextFieldDaysToKeepLogs().requestFocus();
				throw e2;
			} else {
				MainLogger.getInstance()
						.setMaxDaysToKeepLogs(IPMonitorProperties.OPTIONS_MONITOR_MAX_DAYS_TO_KEEP_LOGS_VALUE);
			}
		}
	}

	private void saveSettings() {
		MainLogger.getInstance().deleteOldFiles();
		new IPMonitorPropertiesManager(ipMonitor).saveToFile();
		try {
			if (ServiceManager.getInstance().getService().isRunning()) {
				int answer = JOptionPane.showConfirmDialog(null,
						"The IP Monitor service is currently running. In order to apply the current settings\n"
								+ "the service must be restarted. Do you want to restart the service now?",
						"Service restart confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (answer == JOptionPane.YES_OPTION) {
					ServiceManager.getInstance().getService().stop();
					ServiceManager.getInstance().getService().start();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class JButtonAudioConfiguration implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			new AudioConfigurationController(optionsView);
		}
	}

	private class JButtonAudioTest implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			try {
				AudioPerformer.getInstance().play();
			} catch (Exception e) {
				JOptionPane
						.showMessageDialog(null,
								"An error has been detected while opening file\n"
										+ AudioConfiguration.getInstance().getFileName(),
								"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private class JButtonMailConfiguration implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			new MailConfigurationController(optionsView);
		}
	}

	private class JButtonMailTest implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			JOptionPane.showMessageDialog(null,
					"This test might take a few minutes depending on network\n"
							+ "congestion and email notification configuration.\n\n"
							+ "Close this dialog to start the test.",
					"Please wait", JOptionPane.INFORMATION_MESSAGE);
			try {
				MailPerformer.getInstance().sendMail(ipMonitor.getLastIP(), "[NEW_IP_HERE]");
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null,
						"An error has been detected while trying to send an email.\n\n"
								+ "Check the network settings and the email notification configuration.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private class JButtonVisualConfiguration implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			new VisualConfigurationController(optionsView);
		}
	}

	private class JButtonVisualTest implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			VisualPerformer.getInstance().displayMessage(ipMonitor.getLastIP(), "[NEW_IP_HERE]");
		}
	}

	private class JButtonCommandConfiguration implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			new CommandConfigurationController(optionsView);
		}
	}

	private class JButtonCommandTest implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			try {
				CommandPerformer.getInstance().executeCommand(ipMonitor.getLastIP(), "[NEW_IP_HERE]");
			} catch (Exception e) {
				JOptionPane
						.showMessageDialog(null,
								"An error has been detected while executing command\n"
										+ CommandConfiguration.getInstance().getCommand(),
								"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private abstract class AbstractJButtonServiceAction implements ActionListener {

		private String title;
		private String errorMessage;

		public AbstractJButtonServiceAction(String title, String errorMessage) {
			this.title = title;
			this.errorMessage = errorMessage;
		}

		public abstract ProcessResult serviceOperation() throws IOException;

		public void actionPerformed(ActionEvent event) {
			try {
				ProcessResult processResult = serviceOperation();
				StringBuffer output = new StringBuffer(processResult.getOutput().trim());
				if (ServiceManager.getInstance().getService().shouldIncludeExitCode()) {
					output.append(System.lineSeparator());
					output.append(System.lineSeparator());
					output.append("Exit code: ");
					output.append(processResult.getExitCode());
				}
				JOptionPane.showMessageDialog(null, output.toString(), this.title, JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, this.errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private class JButtonInstallServiceAction extends AbstractJButtonServiceAction {

		public JButtonInstallServiceAction() {
			super("Installation result", "There has been an error while installing the " + serviceName + ".");
		}

		public ProcessResult serviceOperation() throws IOException {
			return ServiceManager.getInstance().getService().install();
		}
	}

	private class JButtonUninstallServiceAction extends AbstractJButtonServiceAction {

		public JButtonUninstallServiceAction() {
			super("Uninstallation result", "There has been an error while uninstalling the " + serviceName + ".");
		}

		public ProcessResult serviceOperation() throws IOException {
			return ServiceManager.getInstance().getService().uninstall();
		}
	}

	private class JButtonStartServiceAction extends AbstractJButtonServiceAction {

		public JButtonStartServiceAction() {
			super("Start result", "There has been an error while starting the " + serviceName + ".");
		}

		public ProcessResult serviceOperation() throws IOException {
			return ServiceManager.getInstance().getService().start();
		}
	}

	private class JButtonStopServiceAction extends AbstractJButtonServiceAction {

		public JButtonStopServiceAction() {
			super("Stop result", "There has been an error while stopping the " + serviceName + ".");
		}

		public ProcessResult serviceOperation() throws IOException {
			return ServiceManager.getInstance().getService().stop();
		}
	}

	private class JButtonTestServiceAction extends AbstractJButtonServiceAction {

		public JButtonTestServiceAction() {
			super("Test result", "There has been an error while testing the state of the " + serviceName + ".");
		}

		public ProcessResult serviceOperation() throws IOException {
			return ServiceManager.getInstance().getService().status();
		}
	}

	private class JCheckBoxEnableLoggingAction implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			setEnableLogging();
		}
	}

	private class JButtonOkAction implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			JButtonApplyAction applyAction = new JButtonApplyAction();
			applyAction.actionPerformed(null);
			if (applyAction.isEverythingOk()) {
				optionsView.dispose();
			}
		}
	}

	private class JButtonCancelAction implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			optionsView.dispose();
		}
	}

	private class JButtonApplyAction implements ActionListener {

		private boolean isEveryhingOk;

		public void actionPerformed(ActionEvent event) {
			isEveryhingOk = true;
			try {
				setInterval();
			} catch (Exception e) {
				isEveryhingOk = false;
			}
			setAutoStart();
			try {
				setUrl();
			} catch (Exception e) {
				isEveryhingOk = false;
			}
			setAudioNotification();
			setMailNotification();
			setVisualNotification();
			setCommandNotification();
			setLookAndFeel();
			setLogging();
			try {
				setMaxDaysToKeepLogs();
			} catch (Exception e) {
				isEveryhingOk = false;
			}
			saveSettings();
		}

		public boolean isEverythingOk() {
			return isEveryhingOk;
		}
	}
}
