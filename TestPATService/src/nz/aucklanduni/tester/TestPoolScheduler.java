package nz.aucklanduni.tester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import nz.aucklanduni.model.ComponentConfig;
import nz.aucklanduni.model.SubModel;
import nz.aucklanduni.scheduler.PoolScheduler;

public class TestPoolScheduler {
	
	private static final String inputFile = "input/erp-model-unlayered-submodel.txt";

	public static void main(String[] args) {

		Gson gson = new GsonBuilder().create();
		BufferedReader reader = null;
		try {
			// read model
			FileReader fileReader = new FileReader(new File(inputFile));
			reader = new BufferedReader(fileReader);
			
			String line;
			List<SubModel> models = new ArrayList<SubModel>();
			int id =1;
			while ((line = reader.readLine()) != null) {
				List<ComponentConfig> compList = gson.fromJson(line, new TypeToken<List<ComponentConfig>>(){}.getType());
				models.add(new SubModel(id,compList));
				id++;
			}
			 
			// start scheduler
			PoolScheduler scheduler = new PoolScheduler();
			scheduler.init(8, false);
			scheduler.setModels(models);
			scheduler.run();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
