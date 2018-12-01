package fr.iambluedev.orion.web.response;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import lombok.Getter;
import spark.Response;

@Getter
public class JsonResponse {
	
	private Map<String, Object> datas;

	public JsonResponse(Response res) {
		res.type("application/json");
		this.datas = new HashMap<String, Object>();
	}
	
	public JsonResponse add(String key, Object value){
		this.datas.put(key, value);
		return this;
	}
	
	public String get(){
		return new Gson().toJson(this.datas);
	}
	
}
