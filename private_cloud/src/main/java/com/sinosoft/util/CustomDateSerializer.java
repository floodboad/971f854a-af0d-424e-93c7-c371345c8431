package com.sinosoft.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * custom jackson serializer for date type usage:
 * 
 * @JsonSerialize(using = CustomDateSerializer.class)
 * 
 * @author xiangqian
 *
 */
public class CustomDateSerializer extends JsonSerializer<Date> {
	@Override
	public void serialize(Date value, JsonGenerator gen, SerializerProvider arg2) throws IOException,
			JsonProcessingException {

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDate = "";
		if (null != value) {
			formattedDate = formatter.format(value);			
		}

		gen.writeString(formattedDate);
	}
}
