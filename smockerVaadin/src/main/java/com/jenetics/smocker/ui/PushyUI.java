package com.jenetics.smocker.ui;

import com.jenetics.smocker.model.Connection;
import com.vaadin.annotations.Push;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Push
public class PushyUI extends UI {
    VerticalLayout layout  = new VerticalLayout();
    Table table = new Table();
    //DataSeries series = new DataSeries();

    @Override
    protected void init(VaadinRequest request) {
    	setSizeFull();
        setContent(table);
        //table.setColumnHeaders(new String[] { "Id"});

        // Start the data feed thread
        new FeederThread().start();
    }

    class FeederThread extends Thread {
        int count = 0;

        @Override
        public void run() {
            // Update the data for a while
			while (count < 10) {
			    try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			    access(new Runnable() {
			        @Override
			        public void run() {
			        	//layout.addComponent(new Label("Test"));
			        	try {
			        		table.addItem(new Connection());
			        	}
			        	catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        	
			        	//table.refreshRowCache();
			        }
			    });
			}

			// Inform that we have stopped running
			access(new Runnable() {
			    @Override
			    public void run() {
			        setContent(new Label("Done!"));
			    }
			});
        }
    }
}