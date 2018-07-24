package nz.aucklanduni.model;

import java.util.ArrayList;
import java.util.List;

public class SubModel {

	private int id;
	private List<ComponentConfig> components;
	private List<Integer> compids;
	
	private int status;
	private ResultVerification result;
	
	
	
	public SubModel(int id, List<ComponentConfig> components) {
		super();
		this.id = id;
		setComponents(components);
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public List<ComponentConfig> getComponents() {
		return components;
	}
	
	public ComponentConfig getCompById(int id) {
		for(ComponentConfig comp: components) {
			if(comp.getId() == id)
				return comp;
		}
		return null;
	}
	
	public void setComponents(List<ComponentConfig> components) {
		this.components = components;
		compids = new ArrayList<Integer>();
		for(ComponentConfig comp: components ) {
			//System.out.println("		addcompid "+comp.getId());
			compids.add(comp.getId());
		}
	}
	
	public boolean hasComp(int id) {
		return compids.contains(id);
	}
	
	
	
	public void cleanNonCaller() {
		while(hasNonCaller() && components.size()>0) {
			removeNonCaller();
			clean();
		}
	}
	public void removeNonCaller() {
		List<ComponentConfig> componentsCopy = new ArrayList(components);
		for(ComponentConfig comp: componentsCopy ) {
			if(comp.getCalls()==null || comp.getCalls().length==0) {
				//System.out.println("	removing "+comp.getId());
				components.remove(comp);
			}
		}
		this.setComponents(components);
	}
	
	public boolean hasNonCaller() {
		for(ComponentConfig comp: components ) {
			if(comp.getCalls()==null || comp.getCalls().length==0)
				return true;
		}
		return false;
	}
	
	public void setSingleStart() {
		boolean hasCaller = false;
		for(ComponentConfig comp: components ) {
			if(comp.isIsstartcaller() && !hasCaller) {
				hasCaller = true;
			} else
				comp.setIsstartcaller(false);;
				
		}
	}
	
	public boolean hasNoStart() {
		for(ComponentConfig comp: components ) {
			if(comp.isIsstartcaller()) {
				return false;
			}
		}
		return true;
	}
	
	// reset the start components
	public void checkAndSetStart() {
		System.out.println("		checkAndSetStart "+this.id +" size:"+this.components.size());
		if(hasNoStart()) {		
			List<ComponentConfig> longestVisitedComps = new ArrayList<ComponentConfig>();
			ComponentConfig longestVisitedComp = null;
			int longestVisited = 0;
			for(ComponentConfig comp: components) {
				int visited = traverseModel(0,comp);
				System.out.println("			traverse"+comp.getId()+" v:"+visited);
				if(visited > longestVisited) {
					longestVisited = visited;
					longestVisitedComps.clear();
					longestVisitedComps.add(comp);
				} else if(visited == longestVisited) {
					longestVisitedComps.add(comp);
				}
				// catch deadloop, if all component are visisted
				if(longestVisited == components.size())
					break;
			}
			for(ComponentConfig st: longestVisitedComps) {
				st.setIsstartcaller(true);
			}
				
		}
		// find other component that call to the same component that start component calls within model
		List<ComponentConfig> startComps = new ArrayList<ComponentConfig>();
		for(ComponentConfig st: components) {
			if(st.isIsstartcaller())
				startComps.add(st);
		}
		List<Integer> calls = new ArrayList<Integer>();
		for(ComponentConfig st: startComps) {
			if(st.isIsstartcaller() && st.getCalls()!=null) {
				for(int i: st.getCalls())
					for(ComponentConfig eachComp: components) {
						System.out.println("	find comp that call "+i+" "+eachComp.getCalls());
						if(eachComp.getCalls()!=null)
							for(int callee: eachComp.getCalls()) {
								// found component that call to the same target
								if(callee == i)
									eachComp.setIsstartcaller(true);
									break;
							}
					}
				
			}

		}
	}
	
	public int traverseModel(int visited, ComponentConfig comp) {
		visited++;
		if(visited < components.size() && comp.getCalls()!=null && comp.getCalls().length>0) {
			int maxVisited=0;
			for(Integer c: comp.getCalls()) {
				int wayVisited = traverseModel(visited, getCompById(c));
				if(maxVisited < wayVisited)
						maxVisited = wayVisited;
			}
			return maxVisited;
		} else {
			return visited;
		}
		
		
	}

	public void clean() {
		// remove call to other component that does not existed in this submodel
		for(ComponentConfig comp: components ) {
			List<Integer> cleanCalls  = new ArrayList<Integer>();
			if(comp.getCalls()!=null) {
				for(int call:comp.getCalls()) {
					if(hasComp(call)) {
						cleanCalls.add(call);
					}
				}
				int[] result = new int[cleanCalls.size()];
				int i=0;
				for(Integer call: cleanCalls) {
					result[i] = call;
					i++;
				}
				comp.setCalls(result);
			}
			
		}
		// remove duplicate
		List<ComponentConfig> newComponentList = new ArrayList<ComponentConfig>();
		this.compids.clear();
		for(ComponentConfig c:components) {
			if(!compids.contains(c.getId())) {
				newComponentList.add(c);
				compids.add(c.getId());
			}
		}
		this.components = newComponentList;
	}
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public ResultVerification getResult() {
		return result;
	}
	public void setResult(ResultVerification result) {
		this.result = result;
	}
	
	public List<ComponentConfig> getCloneComponents(){
		List<ComponentConfig> cloneList = new ArrayList<ComponentConfig>();
		for(ComponentConfig c: this.components) {
			try {
				cloneList.add(c.clone());
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return cloneList;
	}

	
	
}
