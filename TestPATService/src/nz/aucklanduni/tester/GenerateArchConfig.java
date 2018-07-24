package nz.aucklanduni.tester;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import nz.aucklanduni.model.ComponentConfig;
import nz.aucklanduni.util.RandomUtil;

public class GenerateArchConfig {
	private static final int MAX_START = 2;
	private static final int NUMBER_OF_CONFIG = 20;
	
	private static final int N = 10;
	private static final int connectNo = 2;
	public static void main(String[] args) {
		BufferedWriter writer = null;

		Gson gson = new GsonBuilder().create();
		try {
			
			for(int i=0; i<NUMBER_OF_CONFIG; i++) {
				List<ComponentConfig> archConfig1 = randomCompList(N,0, connectNo);
				List<ComponentConfig> archConfig2 = randomCompList(N,0, connectNo);
				List<ComponentConfig> archConfig3 = randomCompList(N,0, connectNo);
				List<ComponentConfig> archConfig4 = randomCompList(N,0, connectNo);
				List<ComponentConfig> archConfig = new ArrayList<ComponentConfig>();
				archConfig.addAll(archConfig1);
				archConfig.addAll(archConfig2);
				archConfig.addAll(archConfig3);
				//archConfig.addAll(archConfig4);
				//renderGraph(archConfig);
				System.out.println(gson.toJson(archConfig));
				VerifierOnThreadedService verifier = new VerifierOnThreadedService(archConfig,80,true);
				verifier.run();
				while(verifier.isAlive()) {
				//	System.out.println("waiting for result.");
				} 
				if(verifier.getResult()!=null && verifier.getResult().getDiagnosisList().size()>0 ) {
					String archStr = gson.toJson(archConfig);
					writer = new BufferedWriter(new FileWriter("input/arch-"+N+"-"+connectNo+"-layered.txt", true));
					writer.write(archStr+"\n");
					writer.write(gson.toJson(archConfig1)+"\n");
					writer.write(gson.toJson(archConfig2)+"\n");
					writer.write(gson.toJson(archConfig3)+"\n");
					//writer.write(gson.toJson(archConfig4)+"\n");
					writer.close();
					
					// write arch config 1
					
				} else {
					i--;
				}

			}
		} catch(Exception e) {
			e.printStackTrace();
		}finally {
//			try {
//				writer.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	
	}

	private static List<ComponentConfig> randomCompList(int N, int minConnect ,int maxConnect) {
		RandomUtil randUtil = new RandomUtil();
		List<ComponentConfig> compList = new ArrayList<ComponentConfig>();
		for (int i = 0; i < N; i++) {
			ComponentConfig comp = new ComponentConfig();
			comp.setId(randUtil.randomNumber(100, 999));
			compList.add(comp);
		}
		List<Integer> calledComp = new ArrayList<Integer>();
		for (ComponentConfig comp : compList) {
			// random number of connection
			int connectNo = randUtil.randomNumber(minConnect, maxConnect);
			List<Integer> calls = new ArrayList<Integer>();
			for (int i = 0; i < connectNo; i++) {
				ComponentConfig connectTo;
				int j = 0;
				do {
					connectTo = compList.get(randUtil.randomNumber(0, N - 1));
					j++;
				} while (j < N * 2 && (connectTo.getId() == comp.getId()
						|| /* calls.contains(connectTo.getId()) || */ connectTo.hasCallTo(comp.getId())));
				calls.add(connectTo.getId());
			}
			calledComp.addAll(calls);
			int[] callsArray = new int[connectNo];
			for (int i = 0; i < connectNo; i++) {
				callsArray[i] = calls.get(i);

			}
			comp.setCalls(callsArray);

		}
		// select & initial start component
		boolean hasStartComp = false;
		int startinvoke = 0;
		for (ComponentConfig comp : compList) {
			if (!calledComp.contains(new Integer(comp.getId()))) {
				comp.setIsstartcaller(true);
				hasStartComp = true;
				startinvoke++;
				if (startinvoke > MAX_START)
					break;
			}
		}
		if (!hasStartComp) {
			compList.get(randUtil.randomNumber(0, N - 1)).setIsstartcaller(true);
		}

		return compList;
	}
}
