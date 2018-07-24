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

public class TestRunWithThreadedService {
	
	private static final int N = 40;
	private static final int connectNo = 2;
	private static final boolean isSync = true;
	
	public static void main(String[] args) {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		Gson gson = new GsonBuilder().create();
		long startTime = new Date().getTime();
		try {

			//FileReader fileReader = new FileReader(new File("input/arch-"+N+"-"+connectNo+".txt"));
			//FileReader fileReader = new FileReader(new File("input/erp-model-unlayered-submodel.txt"));
			FileReader fileReader = new FileReader(new File("input/ec-model-submodel.txt"));
			 reader = new BufferedReader(fileReader);
			//writer = new BufferedWriter(new FileWriter("result/result-arch-"+N+"-"+connectNo+".csv", true));
			 writer = new BufferedWriter(new FileWriter("result/result-ec-model-submodel.csv", false));
		   
			List<VerifierOnThreadedService> verifierList = new ArrayList<VerifierOnThreadedService>();
			String line = "";
			while ((line = reader.readLine()) != null) {
				List<ComponentConfig> compList = gson.fromJson(line, new TypeToken<List<ComponentConfig>>(){}.getType());
				
				// call verifier
				VerifierOnThreadedService verifier = new VerifierOnThreadedService(compList, 80,isSync);
				verifier.start();
				verifierList.add(verifier);
				while(verifier.isAlive()) {}
				if(verifier.getResult()!=null) {
					List<ResultSymptom> result = verifier.getResult().getDiagnosisList();
					
					double memory = 0;
					float time = 0f;
					int state = 0;
					List<String> symptom  = new ArrayList<String>();
					List<String> scenario =  new ArrayList<String>();
					for(ResultSymptom rsItem: result) {
						time += rsItem.getTotalTime();
						memory += rsItem.getMemoryUsage();
						state += rsItem.getNumberOfStates();
						
						if(!rsItem.getSymptom().equals("normal")) {
							if(!scenario.contains(rsItem.getScenario())) {
								symptom.add(rsItem.getSymptom());
								scenario.add(rsItem.getScenario());
							}
								
							
						}
					}
					
					int avgState = Math.round(state/result.size());
					double avgMemory = memory / result.size();
					float avgTime = time / result.size();
					
					String resStr = gson.toJson(result);
					/**
					System.out.println(" ========================================================");
					System.out.println("Response: "+ resStr);
					System.out.println("totalMemory "+ memory);
					System.out.println("totalTime "+ time);
					System.out.println("Average Memory "+ avgMemory);
					System.out.println("Average Time "+ avgTime);
					System.out.println("Average Number of Visited State " + avgState);
					**/
					
					DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
					writer.write(verifier.getComp().size()+";");
					writer.write(gson.toJson(symptom)+";");
					writer.write(df.format(memory)+";");
					writer.write(verifier.getResult().getElapseTime()+";");
					writer.write(df.format(avgMemory)+";");
					writer.write(avgTime+";");
					writer.write(avgState+";");
					String reqStr = gson.toJson(verifier.getComp());
					System.out.println("Request: "+ reqStr);
					writer.write(reqStr+";");
					writer.write(gson.toJson(scenario)+"\n");
					
				}  else {
					writer.write("error;;;;;;;");
					writer.write(gson.toJson(verifier.getComp())+"\n");
				}
			}
			fileReader.close();

			long elapseTime = new Date().getTime() - startTime;
			System.out.println("Elapse Time: "+elapseTime);
			
		} catch (IOException e) {
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
