package com.jenetics.smocker.ui.dialog;

import org.jboss.logging.Logger;

import de.steinwedel.messagebox.MessageBox;

public class Dialog {

	private static Logger logger = Logger.getLogger(Dialog.class);
	
	private Dialog() {
		super();
	}


	public static void ask(String question, String caption, DialogProcess yesProcess, DialogProcess noProcess) {
		MessageBox.createQuestion().withCaption(caption).withMessage(question).withYesButton(() -> {
			if (yesProcess != null) {
				try {
					yesProcess.process();
				} catch (Exception e) {
					logger.error("Unable to process yes", e);
				}
			}
		}).withNoButton(() -> {
			if (noProcess != null) {
				try {
					noProcess.process();
				} catch (Exception e) {
					logger.error("Unable to process no", e);
				}
			}
		}).open();
	}

}
