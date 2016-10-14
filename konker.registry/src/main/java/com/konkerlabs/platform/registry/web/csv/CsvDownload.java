package com.konkerlabs.platform.registry.web.csv;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.io.dozer.ICsvDozerBeanWriter;
import org.supercsv.prefs.CsvPreference;

public class CsvDownload<T> {
	
	public void download(List<T> data, String fileName, HttpServletResponse response, Class<T> clazz) throws IOException, SecurityException, NoSuchMethodException {
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", fileName);

		response.setContentType("text/csv");
		response.setHeader(headerKey, headerValue);
		
		String[] header = createHeader(clazz);
		
		ICsvDozerBeanWriter csvWriter = new CsvDozerBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
		csvWriter.configureBeanMapping(clazz, header);
		csvWriter.writeHeader(header);
		
		for (T t : data) {
			csvWriter.write(t);
		}
		
		csvWriter.close();
	}

	private String[] createHeader(Class<T> clazz) throws  SecurityException, NoSuchMethodException {
		List<String> listHeader = new ArrayList<>();
		
		for (Field field : clazz.getDeclaredFields()) {
			String fieldName = field.getName();
			fieldName = fieldName.substring(0, 1).toUpperCase().concat(fieldName.substring(1));
			
			Method method = clazz.getDeclaredMethod("get".concat(fieldName));
			
			if (method.getReturnType().getCanonicalName().contains("com.konkerlabs.platform.registry")) {
				for (Field localField : method.getReturnType().getDeclaredFields()) {
					listHeader.add(fieldName.concat("." + localField.getName()));
				}
			} else {
				listHeader.add(fieldName);
			}
		}
		
		return listHeader.toArray(new String[0]);
	}

}
