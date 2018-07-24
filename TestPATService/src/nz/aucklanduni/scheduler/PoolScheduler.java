package nz.aucklanduni.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import nz.aucklanduni.model.SubModel;
import nz.aucklanduni.tester.VerifierOnThreadedService;

public class PoolScheduler {
	
	private static final int MAX_POOL = 8;
	private  boolean isSync;
	
	private List<ServiceRunner> servicePool;
	
	private List<SubModel> models; 
	
	private Stack<SubModel> tasks;
	
	private int nextAvailablePool = 0;

	public void init(int poolNumber, boolean isSync) throws Exception{
		if(poolNumber > MAX_POOL)
			throw new Exception("Can not initial more than MAX_POOL "+MAX_POOL);
		this.isSync = isSync;
		servicePool = new ArrayList<ServiceRunner>();
		for(int i=0; i < poolNumber; i++) {
			servicePool.add(new ServiceRunner(80+i));
		}
	}
	
	public void run() {
		
		while(!tasks.isEmpty()) {
			// get available pool
			SubModel runningMod = tasks.pop();
			ServiceRunner serve = allocate();
			serve.setModel(runningMod);
			serve.setSync(isSync);
			serve.execute();
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
				if(!t.getThread().isAlive())
					t.setAvailable(true);
			}
		}
	}

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

	

}
