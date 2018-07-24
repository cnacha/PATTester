package nz.aucklanduni.tester;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import nz.aucklanduni.model.ComponentConfig;
import nz.aucklanduni.model.ResultSymptom;
import nz.aucklanduni.util.RandomUtil;

public class TestRunWithPoolOnLayered {
	
	private static final int N = 5;
	private static final int connectNo = 2;
	private static final boolean isSync = true;
	private static final int numberOfModule = 3;
	public static void main(String[] args) {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		Gson gson = new GsonBuilder().create();
		long startTime = new Date().getTime();
		try {

			//FileReader fileReader = new FileReader(new File("input/arch-"+N+"-"+connectNo+"-layered.txt"));
			FileReader fileReader = new FileReader(new File("input/erp-model.txt"));
			reader = new BufferedReader(fileReader);
			//writer = new BufferedWriter(new FileWriter("result/result-arch-"+N+"-"+connectNo+"-layered.csv", true));
			writer = new BufferedWriter(new FileWriter("result/erp-model.csv", true));
		   
			List<List<VerifierOnThreadedService>> verifierList = new ArrayList<List<VerifierOnThreadedService>>();
			String line = "";
			int i = 0;
			
			while ((line = reader.readLine()) != null) {
				
				List<ComponentConfig> compList = gson.fromJson(line, new TypeToken<List<ComponentConfig>>(){}.getType());
				if(compList.size() != N) {
					List<VerifierOnThreadedService> setList = new ArrayList<VerifierOnThreadedService>();
					for(int j=0; j< numberOfModule; j++) {
						if((line = reader.readLine()) !=null) {
							compList = gson.fromJson(line, new TypeToken<List<ComponentConfig>>(){}.getType());
							// call verifier
							VerifierOnThreadedService verifier = new VerifierOnThreadedService(compList, 80 +j,isSync);
							verifier.start();
							
							setList.add(verifier);
						}
					}
					verifierList.add(setList);
					boolean allFinished = false;
					while(!allFinished) {
						allFinished = true;
						for(VerifierOnThreadedService verifier : setList) {
							if(verifier.isAlive()) {
								allFinished = false;
							}	
						}
					}
					float time = 0f;
					int state = 0;
					for(VerifierOnThreadedService verifier : setList) {
						if(verifier.getResult()!=null) {
							List<ResultSymptom> result = verifier.getResult().getDiagnosisList();
							
							
							time +=verifier.getResult().getElapseTime();
							for(ResultSymptom rsItem: result) {
								
								state += rsItem.getNumberOfStates();
							}
							
						} 
						
					}
					DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
					writer.write(compList.size()+";");
					writer.write(time+";");
					writer.write(state+";");
					String reqStr = gson.toJson(compList);
					System.out.println("Request: "+ reqStr);
					writer.write(reqStr+"\n");
					
					long elapseTime = new Date().getTime() - startTime;
					System.out.println("Elapse Time: "+elapseTime);
				}	
				i++;
			}
			fileReader.close();
			// wait for all thread to finished
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if(writer!=null) {
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
	}

}
