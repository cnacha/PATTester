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

public class TestRunWithPools {
	
	private static final int MAX_START = 2;
	private static final int ARCH_CONFIG_NUM = 10;
	private static final boolean isSync = true;

	public static void main(String[] args) {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		Gson gson = new GsonBuilder().create();
		long startTime = new Date().getTime();
		try {
			int N = 20;
			int connectNo = 2;
			//FileReader fileReader = new FileReader(new File("input/arch-"+N+"-"+connectNo+".txt"));
			FileReader fileReader = new FileReader(new File("input/erp-model.txt"));
			 reader = new BufferedReader(fileReader);
			writer = new BufferedWriter(new FileWriter("result/erp-model.csv", true));
		   
			List<VerifierOnThreadedService> verifierList = new ArrayList<VerifierOnThreadedService>();
			String line = "";
			int i = 0;
			while ((line = reader.readLine()) != null) {
				List<ComponentConfig> compList = gson.fromJson(line, new TypeToken<List<ComponentConfig>>(){}.getType());
				
				// call verifier
				VerifierOnThreadedService verifier = new VerifierOnThreadedService(compList, 80+i,isSync);
				verifier.start();
				verifierList.add(verifier);
				i++;
			}
			fileReader.close();
			// wait for all thread to finished
			boolean allFinished = false;
			while(!allFinished) {
				allFinished = true;
				for(VerifierOnThreadedService verifier : verifierList) {
					if(verifier.isAlive()) {
						allFinished = false;
					}	
				}
			}
			
			for(VerifierOnThreadedService verifier : verifierList) {
				if(verifier.getResult()!=null) {
					List<ResultSymptom> result = verifier.getResult().getDiagnosisList();
					
					double memory = 0;
					float time = 0f;
					int state = 0;
					
					for(ResultSymptom rsItem: result) {
						time += rsItem.getTotalTime();
						memory += rsItem.getMemoryUsage();
						state += rsItem.getNumberOfStates();
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
					writer.write(result.get(0).getSymptom()+";");
					writer.write(df.format(memory)+";");
					writer.write(verifier.getResult().getElapseTime()+";");
					writer.write(state+";");
					writer.write(time+";");
					writer.write(df.format(avgMemory)+";");
					writer.write(avgTime+";");
					writer.write(avgState+";");
					String reqStr = gson.toJson(verifier.getComp());
					System.out.println("Request: "+ reqStr);
					writer.write(reqStr+"\n");
					
				} 
				
			}
			long elapseTime = new Date().getTime() - startTime;
			System.out.println("Elapse Time: "+elapseTime);
			
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
	
	private static List<ComponentConfig> randomCompList(int N, int maxConnect){
		RandomUtil randUtil = new RandomUtil();
		List<ComponentConfig> compList = new ArrayList<ComponentConfig>();
		for(int i=0; i<N; i++) {
			ComponentConfig comp = new ComponentConfig();
			comp.setId(randUtil.randomNumber(100,999));
			compList.add(comp);
		}
		List<Integer> calledComp = new ArrayList<Integer>();
		for(ComponentConfig comp: compList) {
			int connectNo = randUtil.randomNumber(0, maxConnect);
			List<Integer> calls = new ArrayList<Integer>();
			for(int i=0; i<connectNo; i++) {
				ComponentConfig connectTo;
				int j= 0;
				do {
					connectTo = compList.get(randUtil.randomNumber(0, N-1));
					j++;
				}while(j<N*2 && (connectTo.getId() == comp.getId() || /*calls.contains(connectTo.getId()) ||*/ connectTo.hasCallTo(comp.getId())));
				calls.add(connectTo.getId()) ;
			}
			calledComp.addAll(calls);
			int[] callsArray = new int[connectNo];
			for(int i=0; i<connectNo; i++) {
				callsArray[i] = calls.get(i);
				
			}
			comp.setCalls(callsArray);
			
		}
		// select & initial start component
		boolean hasStartComp = false;
		int startinvoke = 0;
		for(ComponentConfig comp: compList) {
			if(!calledComp.contains(new Integer(comp.getId()))) {
				comp.setIsstartcaller(true);
				hasStartComp = true;
				startinvoke++;
				if(startinvoke > MAX_START)
					break;
			}
		}
		if(!hasStartComp) {
			compList.get(randUtil.randomNumber(0, N-1)).setIsstartcaller(true);
		}

		
		
		return compList;
	}

}
