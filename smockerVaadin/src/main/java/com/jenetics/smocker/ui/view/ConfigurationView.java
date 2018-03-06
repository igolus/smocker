package com.jenetics.smocker.ui.view;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.persistence.internal.libraries.asm.Label;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridBasedCrudComponent;
import org.vaadin.crudui.form.CrudFormFactory;
import org.vaadin.crudui.form.impl.GridLayoutCrudFormFactory;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.model.config.SmockerConf;
import com.jenetics.smocker.network.ClientCommunicator;
import com.jenetics.smocker.network.util.ReplaceHeaderItem;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.util.SmockerGridBasedCrudComponent;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

@ContentView(sortingOrder = 1, viewName = "ConfigurationView", icon = "icons/Java-icon.png", homeView = true, rootViewParent = ConnectionsRoot.class, 
bundle = "BundleUI")

public class ConfigurationView extends VerticalLayout implements View {
	
	DaoManager<JavaApplication> javaApplicationDaoManager = new DaoManager<JavaApplication>(JavaApplication.class, SmockerUI.getEm());
	
	private Set<ReplaceHeaderItem> source = new LinkedHashSet<ReplaceHeaderItem>();
	
	public ConfigurationView() {
//		
//		source.add(new ReplaceHeaderItem("001", "P001"));
//        source.add(new ReplaceHeaderItem("002", "P002"));
//        source.add(new ReplaceHeaderItem("003", "P003"));
//		
//       GridBasedCrudComponent<ReplaceHeaderItem> gridBasedCrudComponent = 
//        		new GridBasedCrudComponent<ReplaceHeaderItem>(ReplaceHeaderItem.class);
//		
//		gridBasedCrudComponent.setAddOperation(item -> this.add(item));
//		gridBasedCrudComponent.setUpdateOperation(this::update);
//		gridBasedCrudComponent.setDeleteOperation(this::delete);
//		gridBasedCrudComponent.setFindAllOperation(this::findAll);
//		gridBasedCrudComponent.setSizeFull();
//		addComponent(gridBasedCrudComponent);
//       table.setSizeFull();
//       addComponent(table);
		
//		GridCrud<ReplaceHeaderItem> crud = new GridCrud<>(ReplaceHeaderItem.class);
//		
		BeanItemContainer<ReplaceHeaderItem> container = new BeanItemContainer<>(ReplaceHeaderItem.class);
       
        container.addBean(new ReplaceHeaderItem("002", "P002"));
        container.addBean(new ReplaceHeaderItem("003", "P003"));
        
        Table table = new Table();
        table.setImmediate(true);
        table.setContainerDataSource(container);
        table.addGeneratedColumn("edit", new ColumnGenerator() {        
            
            @Override
            public Object generateCell(Table source, Object rowId, Object columnId) {
                Button editButton = new Button(FontAwesome.PENCIL);
                editButton.addClickListener(new ClickListener() {
                    
                    @Override
                    public void buttonClick(ClickEvent event) {
                    	ReplaceHeaderItem currentItem = (ReplaceHeaderItem)rowId;
                        table.setTableFieldFactory(new TableFieldFactory() {
                            
                            @Override
                            public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                            	ReplaceHeaderItem item = (ReplaceHeaderItem)itemId;
                                if(currentItem.equals(item)) {
                                	
									if (propertyId.toString().equals("regExp")) {
										TextField textField = new TextField();
                                		return textField;
                                    }
                                	if (propertyId.toString().equals("ReplaceValue")) {
                                		TextField textField = new TextField();
                                        return textField;
                                    }
                                }
                                
                                return null;
                            }
                        }); //end of set table field factory
                        table.setEditable(true);
                    } //end of button click
                });
                        
                return editButton;
            } //end of generate cell
        });   
        
        
        table.addGeneratedColumn("done", new ColumnGenerator() {        
            @Override
            public Object generateCell(Table source, Object rowId, Object columnId) {
                Button editButton = new Button(FontAwesome.CHECK);
                editButton.addClickListener(new ClickListener() {
                    
                    @Override
                    public void buttonClick(ClickEvent event) {
                    	//container.
                    	//ReplaceHeaderItem currentItem = (ReplaceHeaderItem)rowId;
                        //table.setTableFieldFactory(DefaultFieldFactory.get()); //end of set table field factory
                        table.setEditable(false);
                        table.setContainerDataSource(null);
                        table.setContainerDataSource(container);
                        table.refreshRowCache();
                    } //end of button click
                });
                        
                return editButton;
            } //end of generate cell
        });     
        
        
        table.setEditable(false);
        table.setSizeFull();
        addComponent(table);
		
//		Button switchRecordReplay = new Button("REPLAY");
//		switchRecordReplay.addClickListener(new ClickListener() {
//			
//			@Override
//			public void buttonClick(ClickEvent event) {
//				// TODO Auto-generated method stub
//				DaoManager<SmockerConf> daoConf = new DaoManager<SmockerConf>(SmockerConf.class, SmockerUI.getEm());
//				SmockerConf conf = daoConf.listAll().iterator().next();
//				if (conf.equals("VIEW")) {
//					conf.setMode("REPLAY");
//					switchRecordReplay.setCaption("VIEW");
//				}
//				else {
//					conf.setMode("VIEW");
//					switchRecordReplay.setCaption("VIEW");
//				}
//				daoConf.update(conf);
//				
//				
//				List<JavaApplication> allJavaApp = javaApplicationDaoManager.listAll();
//				if (allJavaApp.size() > 0) {
//					ClientCommunicator.sendMode(conf.getMode(), allJavaApp.get(0));
//				}
//			}
//		});
//		
//	   addComponent(switchRecordReplay);
		// TODO Auto-generated constructor stub
	}

	private ReplaceHeaderItem add(ReplaceHeaderItem item) {
		// TODO Auto-generated method stub
		source.add(item);
		return item;
	}
	
	private ReplaceHeaderItem update(ReplaceHeaderItem item) {
		// TODO Auto-generated method stub
		return item;
	}
	
	private void delete(ReplaceHeaderItem item) {
		// TODO Auto-generated method stub
		source.remove(item);
	}
	
	private Set<ReplaceHeaderItem> findAll() {
		// TODO Auto-generated method stub
		return source;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	
}
