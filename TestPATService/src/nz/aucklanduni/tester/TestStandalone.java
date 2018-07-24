package nz.aucklanduni.tester;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import nz.aucklanduni.model.ComponentConfig;
import nz.aucklanduni.model.ResultSymptom;

public class TestStandalone {

	public static void main(String[] args) {
		HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead 

		try {

		    HttpPost request = new HttpPost("http://fasad.cer.auckland.ac.nz/api/patweb/verify");
		    
		    List<ComponentConfig> listComp = new ArrayList<ComponentConfig>();
		    
		    ComponentConfig comp1Config = new ComponentConfig();
		    comp1Config.setId(101);
		    comp1Config.setIsstartcaller(true);
		    comp1Config.setCalls(new int[] {102});
		    listComp.add(comp1Config);
		    
		    ComponentConfig comp2Config = new ComponentConfig();
		    comp2Config.setId(102);
		    comp2Config.setCalls(new int[] {103,104});
		    listComp.add(comp2Config);
		    
		    ComponentConfig comp3Config = new ComponentConfig();
		    comp3Config.setId(103);
		    comp3Config.setCalls(new int[] {101,106});
		    listComp.add(comp3Config);
		    
		    ComponentConfig comp4Config = new ComponentConfig();
		    comp4Config.setId(104);
		    comp4Config.setCalls(new int[] {105});
		    listComp.add(comp4Config);
		    
		    ComponentConfig comp5Config = new ComponentConfig();
		    comp5Config.setId(105);
		    comp5Config.setCalls(new int[] {102});
		    listComp.add(comp5Config);
		    
		    ComponentConfig comp6Config = new ComponentConfig();
		    comp6Config.setId(106);
		    comp6Config.setCalls(new int[] {105});
		    listComp.add(comp6Config);
		    
		    Gson gson = new GsonBuilder().create();
		    String reqStr = gson.toJson(listComp);
		    System.out.println(reqStr);
		    
		    StringEntity params =new StringEntity(reqStr);
		    request.addHeader("content-type", "application/json");
		    request.setEntity(params);
		    HttpResponse response = httpClient.execute(request);

		    System.out.println(response.getStatusLine().toString());
		    HttpEntity entity = response.getEntity();
		    String resString = EntityUtils.toString(entity, "UTF-8");
		    // System.out.println(resString);
		    
		    Type listType = new TypeToken<ArrayList<ResultSymptom>>(){}.getType();
		    List<ResultSymptom> list = gson.fromJson(resString, listType);
		    System.out.println(list.get(1).getScenario().trim());
		    

		}catch (Exception ex) {

		    ex.printStackTrace();

		} finally {
		    //Deprecated
		    //httpClient.getConnectionManager().shutdown(); 
		}	

	}

}
