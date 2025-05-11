package com.ocado.paymentoptimiser;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.ocado.paymentoptimiser.model.ISerializable;
import com.ocado.paymentoptimiser.service.JsonReader;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.List;

public class JsonReaderTest {

    private JsonReader<TestJson> jsonReader  = new JsonReader<>();

    @Test
    public void testSuccess() throws JsonIOException, JsonSyntaxException, FileNotFoundException {

        List<TestJson> result = jsonReader.readJson(Paths.get("src/test/resources/json_reader.json").toString(), TestJson.class);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test", result.get(0).testString);
    }

    @Test
    public void testFileNotFound() {
    	assertThrows(FileNotFoundException.class, () -> jsonReader.readJson("non_existing_file.json", TestJson.class));
    }

    @Test
    public void testJsonSyntaxError() {
    	assertThrows(JsonSyntaxException.class, () -> jsonReader.readJson(Paths.get("src/test/resources/json_invalid_syntax.json").toString(), TestJson.class));
    }
    
    @AllArgsConstructor
    public class TestJson implements ISerializable {
    	public String testString;
    	public List<String> testList;
    	public String testBigDecimal;
    	public BigDecimal testBigDecimalAsBigDecimal;
		@Override
		public void afterDeserialization() {
			testBigDecimalAsBigDecimal = new BigDecimal(testBigDecimal);
		}
    	
    }
    
}
