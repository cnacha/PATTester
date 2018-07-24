package nz.aucklanduni.tester;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import nz.aucklanduni.model.ComponentConfig;
import nz.aucklanduni.model.ResultSymptom;

public class ArchVerifier{
	
	List<ComponentConfig> comp;
	List<ResultSymptom> result;
	
	
	 public ArchVerifier(List<ComponentConfig> comp) {
		super();
		this.comp = comp;
	}
	 
	public List<ComponentConfig> getComp() {
		return comp;
	}



	public void setComp(List<ComponentConfig> comp) {
		this.comp = comp;
	}
	
	public List<ResultSymptom> getResult() {
		return result;
	}

	public void setResult(List<ResultSymptom> result) {
		this.result = result;
	}

	public List<ResultSymptom> run(){
		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost("http://fasad.cer.auckland.ac.nz/api/patweb/verify");
			
			Gson gson = new GsonBuilder().create();
			String reqStr = gson.toJson(comp);
			//System.out.println(reqStr);
			
			StringEntity params =new StringEntity(reqStr);
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse response = httpClient.execute(request);

			//System.out.println(response.getStatusLine().toString());
			HttpEntity entity = response.getEntity();
			String resString = EntityUtils.toString(entity, "UTF-8");
			//System.out.println("result: "+resString);
			
			
			if(resString!=null && !resString.equals("") && resString.indexOf("ExceptionMessage")==-1 ) { 
				Type listType = new TypeToken<ArrayList<ResultSymptom>>(){}.getType();
				List<ResultSymptom> list = gson.fromJson(resString, listType);
				return list;
			}
			else
				return null;
			
			//System.out.println(list.get(1).getScenario().trim());
			
			
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
