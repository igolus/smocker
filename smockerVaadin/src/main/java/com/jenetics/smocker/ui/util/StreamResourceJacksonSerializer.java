package com.jenetics.smocker.ui.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jenetics.smocker.model.Scenario;
import com.jenetics.smocker.ui.util.JsonFileDownloader.OnDemandStreamResource;

public class StreamResourceJacksonSerializer {
	
	@Inject
	private static Logger logger;
	
	public static OnDemandStreamResource getStreamResource(Object pojo, String fileName) {
		return new OnDemandStreamResource() {
			@Override
			public InputStream getStream() {
				try {
					if (pojo != null) {
						ObjectMapper mapper = new ObjectMapper();
						mapper.enable(SerializationFeature.INDENT_OUTPUT);
						mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
						mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
						String jsonObjSTring = 
								mapper.writerWithDefaultPrettyPrinter().writeValueAsString(pojo);
						return new ByteArrayInputStream(jsonObjSTring.getBytes("UTF-8"));
					}
				} catch (IOException e) {
					logger.error("Unable to export " + pojo.getClass().getCanonicalName(), e);
				}
				return null;
			}
	
			@Override
			public String getFilename() {
				return fileName;
			}
	
		};
	}
	
	
	
//	return new OnDemandStreamResource() {
//		@Override
//		public InputStream getStream() {
//			try {
//				Scenario scenario = getSelectedScenario();
//				if (scenario != null) {
//					ObjectMapper mapper = new ObjectMapper();
//					mapper.enable(SerializationFeature.INDENT_OUTPUT);
//					mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
//					mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
//					String jsonObjSTring = 
//							mapper.writerWithDefaultPrettyPrinter().writeValueAsString(scenario);
//					return new ByteArrayInputStream(jsonObjSTring.getBytes("UTF-8"));
//				}
//			} catch (IOException e) {
//				logger.error("Unable to export scenario", e);
//			}
//			return null;
//		}
//
//		@Override
//		public String getFilename() {
//			return getSelectedScenario().getName() + ".json";
//		}
//
//	};
}
