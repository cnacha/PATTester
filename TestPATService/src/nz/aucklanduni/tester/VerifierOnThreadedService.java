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
import nz.aucklanduni.model.ResultVerification;

public class VerifierOnThreadedService extends Thread{
	
	List<ComponentConfig> comp;
	ResultVerification result;
	int port;
	boolean sync = true;
	
	
	
	 public VerifierOnThreadedService(int port, boolean sync) {
		super();
		this.port = port;
		this.sync = sync;
	}

	public VerifierOnThreadedService(List<ComponentConfig> comp, int port) {
		super();
		this.comp = comp;
		this.port = port;
	}
	 
	 public VerifierOnThreadedService(List<ComponentConfig> comp, int port, boolean isSync) {
			super();
			this.comp = comp;
			this.port = port;
			this.sync = isSync;
		}


	public void run(){
	       System.out.println("ArchVerifier started");
	       result = callService();
	 }
	 
	public List<ComponentConfig> getComp() {
		return comp;
	}

	public void setComp(List<ComponentConfig> comp) {
		this.comp = comp;
	}
	


	public boolean isSync() {
		return sync;
	}

	public void setSync(boolean sync) {
		this.sync = sync;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ResultVerification getResult() {
		return result;
	}

	public void setResult(ResultVerification result) {
		this.result = result;
	}

	public ResultVerification callService(){
		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			String method = "sync";
			if(!this.sync) {
				method = "async";
			}
			HttpPost request = new HttpPost("http://fasad.cer.auckland.ac.nz:"+port+"/api/pattest/"+method+"verify");
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
			System.out.println("result from "+port+": "+resString);
						
			if(resString!=null && !resString.equals("") && resString.indexOf("ExceptionMessage")==-1 ) { 
				ResultVerification result = gson.fromJson(resString, ResultVerification.class);
				return result;
			}
			else
				return null;
						
			
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
