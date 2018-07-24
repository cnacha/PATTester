package nz.aucklanduni.tester;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import nz.aucklanduni.model.ComponentConfig;

public class DependencyNumberFindingParser {
	
	private static final int N = 5;
	private static final int connectNo = 2;
	private static final boolean isSync = true;
	private static final int numberOfModule = 3;

	public static void main(String[] args) {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		Gson gson = new GsonBuilder().create();
		try {

			FileReader fileReader = new FileReader(new File("input/arch-"+N+"-"+connectNo+"-layered.txt"));
			reader = new BufferedReader(fileReader);
			String line = null;
			writer = new BufferedWriter(new FileWriter("result/conn-sum-"+N+"-"+connectNo+"-layered.csv", true));
			while ((line = reader.readLine()) != null) {
				List<ComponentConfig> compList = gson.fromJson(line, new TypeToken<List<ComponentConfig>>(){}.getType());
				if(compList.size() != N) {
					int totalConn = 0;
					for(ComponentConfig comp: compList) {
						totalConn+=comp.getCalls().length;
					}
					writer.write(totalConn+"\n");
				}
			}
			writer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

}
