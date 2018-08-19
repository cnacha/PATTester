package nz.aucklanduni.scheduler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import nz.aucklanduni.model.ResultVerification;
import nz.aucklanduni.model.SubModel;
import nz.aucklanduni.tester.VerifierOnThreadedService;
import nz.aucklanduni.util.RandomUtil;

public class PoolScheduler {
	
	private static final int MAX_POOL = 8;
	private  boolean isSync;
	
	private List<ServiceRunner> servicePool;
	
	private List<SubModel> models; 
	
	private Stack<SubModel> tasks;
	private String testName;
	private int testNo;
	private String fileName;
	private BufferedWriter writer;
	private BufferedWriter resultWriter;
	
	private int nextAvailablePool = 0;
	
	public List<SubModel> getModels() {
		return models;
	}

	public void setModels(List<SubModel> models) {
		this.models = models;
		tasks = new Stack<SubModel>();
		for(int i=models.size()-1; i>=0; i--) {
			tasks.push(models.get(i));
		}
	}

	public void init(String testName,int testNo, int poolNumber, boolean isSync) throws Exception{
		if(poolNumber > MAX_POOL)
			throw new Exception("Can not initial more than MAX_POOL "+MAX_POOL);
		this.isSync = isSync;
		this.testName = testName;
		this.testNo = testNo;
		this.fileName = "result/"+testName+"-"+(isSync?"async":"sync")+"/result"+testNo+".csv";
		File targetFile = new File(fileName);
		File parent = targetFile.getParentFile();
		if (!parent.exists() && !parent.mkdirs()) {
		    throw new IllegalStateException("Couldn't create dir: " + parent);
		}
		servicePool = new ArrayList<ServiceRunner>();
		for(int i=0; i < poolNumber; i++) {
			servicePool.add(new ServiceRunner(80+i));
		}
		writer = new BufferedWriter(new FileWriter(fileName, false));
		resultWriter = new BufferedWriter(new FileWriter( "result/"+testName+"-"+(isSync?"async":"sync")+"/result-summary.csv", true));
	}
	
	public void run() {
		long startTime = new Date().getTime();
		while(!tasks.isEmpty()) {
			// get available pool
			SubModel runningMod = tasks.pop();
			ServiceRunner serve = allocate();
			serve.setModel(runningMod);
			serve.setSync(isSync);
			serve.execute();
		}
		boolean allFinish = false;
		while(!allFinish) {
			allFinish = true;
			System.out.println("waiting for all result");
			for(ServiceRunner t: servicePool) {
				if(t.getThread()!=null ) {
					if(t.getThread().isAlive()) {
						allFinish = false;
						break;
					} else {
						gatherResult(t.getThread());
						t.reset();
					}
				} 
			}
		}
		long elapseTime = new Date().getTime() - startTime;
		System.out.println("Elapse Time: "+elapseTime);
		try {
			writer.write(elapseTime+"\n");
			resultWriter.write(testNo+";"+elapseTime+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				writer.close();
				resultWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public ServiceRunner allocate() {
		// loop though until available service is found
		while(true) {
			for(ServiceRunner t: servicePool) {
				//System.out.println("service on "+t.getPortNumber()+" "+t.isAvailable());
				if( t.isAvailable()) {
					t.setAvailable(false);
					return t;
				} 
			}
			for(ServiceRunner t: servicePool) {
				if(t.getThread()!=null && !t.getThread().isAlive()) {
					t.setAvailable(true);
					// gather information
					gatherResult(t.getThread());
					t.reset();
				}
			}
		}
	}
	
	private void gatherResult(VerifierOnThreadedService thread) {
		try {
			RandomUtil random = new RandomUtil();
			
		
			Gson gson = new GsonBuilder().create();
		    if(thread.getResult()!=null)
		    	writer.write(thread.getResult().getElapseTime()+";"+gson.toJson(thread.getResult().getDiagnosisList())+"\n");
		    else
		    	writer.write("error at"+this.testName+"/"+this.testNo+"\n");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	

	

}
