package com.ocado.paymentoptimiser.service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.ocado.paymentoptimiser.model.ISerializable;

public class JsonReader <T extends ISerializable> {
	
	public static Gson GSON = new Gson();
	
	public List<T> readJson(String filePath, Class<T> clazz) throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		
		Type type = TypeToken.getParameterized(List.class, clazz).getType();
		List<T> list = GSON.fromJson(new FileReader(filePath), type);
		for (T item : list) {
			item.afterDeserialization();
		}
		return list;

	}

	public List<T> readJsonOrNull(String filePath, Class<T> clazz) {
		try {
			return readJson(filePath, clazz);
		} catch (JsonIOException e) {
			System.out.println("Cannot read json file of " + filePath + "!");
		} catch (JsonSyntaxException e) {
			System.out.println("Cannot parse json file " + filePath + "!");
		} catch (FileNotFoundException e) {
			System.out.println("Cannot find json file " + filePath + "!");
		}
		return null;
	}

}
