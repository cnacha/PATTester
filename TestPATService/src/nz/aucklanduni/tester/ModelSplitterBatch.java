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
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import nz.aucklanduni.model.ComponentConfig;
import nz.aucklanduni.model.ResultSymptom;
import nz.aucklanduni.model.SubModel;
import nz.aucklanduni.util.RandomUtil;

public class ModelSplitterBatch {

	private static final int MAX_COMPONENT = 4;

	private static final String testName = "arch-40-2";// "ec-model";

	private static final String inputfile = "input/" + testName + ".txt";
	private static final String outputModelfile = "input/"+testName+"/" + testName + "-submodel";
	private static final String outputResultfile = "result/" + testName + "-monolith.csv";
	// private static final String filename = "input/test-model.txt";
	
	private static int count = 0;

	public static void split(String systemModelStr, BufferedWriter overviewResult) {
		Gson gson = new GsonBuilder().create();
		long startTime = (new Date()).getTime();
		try {
			// read components objects list from file
			List<ComponentConfig> plainCompList = gson.fromJson(systemModelStr, new TypeToken<List<ComponentConfig>>() {
			}.getType());
			// convert to hashtable
			Hashtable<Integer, ComponentConfig> compList = new Hashtable<Integer, ComponentConfig>();
			for (ComponentConfig comp : plainCompList) {
				compList.put(comp.getId(), comp);
			}

			Hashtable<Integer, ComponentConfig> compList2 = new Hashtable<Integer, ComponentConfig>(compList);

			// find sub-models
			// List<List<ComponentConfig>> moduleList = new
			// ArrayList<List<ComponentConfig>>();
			List<SubModel> submodelList = new ArrayList<SubModel>();
			List<ComponentConfig> tmpModule = new ArrayList<ComponentConfig>();
			int modId = 1;
			while (compList.size() > 0) {
				// find start component
				int startComp = 0;
				Set<Integer> keys = compList.keySet();
				for (Integer key : keys) {
					if (compList.get(key).isIsstartcaller()) {
						startComp = key;
						break;
					}
					// System.out.println("Value of "+key+" is: "+hm.get(key));
				}
				if (startComp == 0)
					startComp = compList.keys().nextElement();
				System.out.println(" harvesting -> " + startComp);
				tmpModule = collectComp(compList, tmpModule, startComp, MAX_COMPONENT);
				// clean hashtable
				for (ComponentConfig comp : tmpModule) {
					compList.remove(comp.getId());
				}
				System.out.println(" finish -> " + tmpModule.size() + "  list:" + compList.size());
				if (tmpModule.size() >= MAX_COMPONENT || compList.size() == 0) {
					// moduleList.add(tmpModule);
					submodelList.add(new SubModel(modId, tmpModule));
					modId++;
					tmpModule = new ArrayList<ComponentConfig>();
				}

			}

			// find overlapping model
			List<SubModel> overlapList = new ArrayList<SubModel>();
			List<SubModel> tmpModels = overlapModel(compList2, submodelList);
			overlapList.addAll(tmpModels);
			/*
			 * for(int i=0;i<1; i++) { tmpModels = overlapModel(compList2, tmpModels);
			 * overlapList.addAll(tmpModels); }
			 */
			for (SubModel mod : overlapList) {
				mod.clean();
				System.out.println(mod.getId() + " size:" + mod.getComponents().size());
			}

			/*
			 * for(int i=0; i<submodelList.size();i++) { for(int j=0; j<submodelList.size();
			 * j++) { if(i!=j) { // match calls array with id in the module list by j
			 * List<ComponentConfig> ol =
			 * overlapComp(compList2,submodelList.get(i).getComponents(),submodelList.get(j)
			 * .getComponents() ); if(ol.size()>0) { SubModel mod = new SubModel(modId,ol);
			 * mod.clean(); overlapList.add(mod); modId++; } } } }
			 */

			// construct overview model
			List<ComponentConfig> overviewComps = new ArrayList<ComponentConfig>();
			for (SubModel mod : submodelList) {
				ComponentConfig comp = new ComponentConfig();
				comp.setId(mod.getId());
				List<Integer> calls = findRelatedModels(mod, submodelList);
				if (calls.size() > 0) {
					int[] result = new int[calls.size()];
					int i = 0;
					for (Integer call : calls) {
						// System.out.print(" "+call);
						result[i] = call;
						i++;
					}
					// System.out.println("\n"+result.length);
					comp.setCalls(result);
				}
				overviewComps.add(comp);
				// System.out.println("====================================");
			}

			SubModel overviewModel = new SubModel(9999, overviewComps);

			// verify overview model
			int prevStartIndex = 0;
			List<VerifierOnThreadedService> verifierList = new ArrayList<VerifierOnThreadedService>();
			System.out.println("=========== Overview Model===============");
			for (int i = 0; i < overviewModel.getComponents().size(); i++) {
				if (overviewModel.getComponents().get(i).getCalls() != null
						&& overviewModel.getComponents().get(i).getCalls().length > 0) {
					overviewModel.getComponents().get(prevStartIndex).setIsstartcaller(false);
					overviewModel.getComponents().get(i).setIsstartcaller(true);
					prevStartIndex = i;
					System.out.println(gson.toJson(overviewModel.getComponents()));
					VerifierOnThreadedService verifier = new VerifierOnThreadedService(overviewModel.getComponents(),
							80 + i, true);
					verifier.start();
					verifierList.add(verifier);
				}

			}
			// wait for result
			boolean allFinished = false;
			while (!allFinished) {
				allFinished = true;
				for (VerifierOnThreadedService verifier : verifierList) {
					if (verifier.isAlive()) {
						allFinished = false;
					}
				}
			}

			// analyse result
			float time = 0f;
			int state = 0;
			String symptom = "normal";
			List<ResultSymptom> defectResultList = new ArrayList<ResultSymptom>();
			List<String> detectScenarioList = new ArrayList<String>();
			for (VerifierOnThreadedService verifier : verifierList) {
				if (verifier.getResult() != null) {
					List<ResultSymptom> result = verifier.getResult().getDiagnosisList();

					time = verifier.getResult().getElapseTime();
					String scenario = "";
					// System.out.println(gson.toJson(result));
					for (ResultSymptom rsItem : result) {

						state = rsItem.getNumberOfStates();
						if (rsItem.getSymptom().equals("deadloop") || rsItem.getSymptom().equals("livelock")) {

							symptom = rsItem.getSymptom();
							scenario = rsItem.getScenario();
							if (!detectScenarioList.contains(rsItem.getScenario())) {
								defectResultList.add(rsItem);
								detectScenarioList.add(rsItem.getScenario());
							}
						}

					}

					overviewResult.write(symptom + ";");
					overviewResult.write(time + ";");
					overviewResult.write(state + ";");
					overviewResult.write(gson.toJson(verifier.getComp()) + ";");
					overviewResult.write(scenario + "\n");

				}
			}

			System.out.println("============================================= ");
			// construct merge submodels
			List<SubModel> mergeModels = new ArrayList<SubModel>();
			for (ResultSymptom result : defectResultList) {
				// extract model id from detected scenario
				System.out.println("Detected: " + gson.toJson(result));
				StringTokenizer st = new StringTokenizer(result.getScenario());
				List<Integer> mergeModelIds = new ArrayList<Integer>();
				while (st.hasMoreElements()) {
					String eventStr = (String) st.nextElement();
					if (eventStr.indexOf("invoke") != -1) {
						int modelId = Integer.parseInt(eventStr.substring(eventStr.indexOf(".") + 1));
						if (!mergeModelIds.contains(modelId))
							mergeModelIds.add(modelId);
					}
				}
				SubModel mergedModel = mergeSubModel(mergeModelIds, submodelList);
				if (result.getSymptom().equals("deadloop")) {

					mergedModel.cleanNonCaller();
					// System.out.println("merged model "+gson.toJson(mergedModel));
					mergedModel.setSingleStart();

				} else
					mergedModel.clean();
				System.out.println("merged model " + mergedModel.getId() + ":" + gson.toJson(mergedModel));
				if (mergedModel.getComponents().size() > 0)
					mergeModels.add(mergedModel);
			}

			submodelList.addAll(overlapList);

			// check for non caller model
			for (SubModel mod : submodelList) {
				mod.clean();
				mod.checkAndSetStart();
			}

			submodelList.addAll(mergeModels);

			for (SubModel mod : submodelList) {
				System.out.println(gson.toJson(mod));
			}

			printModels(submodelList);
			writeModels(submodelList, outputModelfile+(++count)+".txt");
			overviewResult.write((count-1)+","+ ((new Date()).getTime() - startTime) +"\n");
			// printModelsJson(submodelList);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			FileReader fileReader = new FileReader(new File(inputfile));
			reader = new BufferedReader(fileReader);
			writer = new BufferedWriter(new FileWriter(outputResultfile, false));
			String line;
			while ((line = reader.readLine()) != null) {
				split(line, writer);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
				if (writer != null)
					writer.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static SubModel mergeSubModel(List<Integer> modids, List<SubModel> modelList) {
		List<ComponentConfig> compList = new ArrayList<ComponentConfig>();
		Gson gson = new GsonBuilder().create();
		for (SubModel mod : modelList) {
			if (modids.contains(mod.getId()) && !compList.contains(mod.getComponents())) {
				System.out.println("	merging " + gson.toJson(mod.getComponents()));
				compList.addAll(mod.getCloneComponents());
			}
		}
		RandomUtil rand = new RandomUtil();
		return new SubModel(rand.randomNumber(1000, 9999), compList);
	}

	private static List<Integer> findRelatedModels(SubModel model, List<SubModel> modelList) {
		List<Integer> calls = new ArrayList<Integer>();

		for (ComponentConfig comp : model.getComponents()) {
			if (comp.getCalls() != null) {
				for (int callTo : comp.getCalls()) {
					if (!model.hasComp(callTo)) {
						// calling component, which not in the model
						// identify call to other model
						for (SubModel eModel : modelList) {
							if (!calls.contains(eModel.getId()) && eModel.hasComp(callTo)) {
								System.out.println("	inserting modelid" + eModel.getId() + " to " + model.getId());
								calls.add(eModel.getId());
							}
						}
					}
				}
			}

		}

		return calls;
	}

	private static List<ComponentConfig> overlapComp(Hashtable<Integer, ComponentConfig> compList,
			List<ComponentConfig> a, List<ComponentConfig> b) throws CloneNotSupportedException {
		List<ComponentConfig> result = new ArrayList<ComponentConfig>();

		for (ComponentConfig compAmodule : a) {
			for (ComponentConfig compBmodule : b) {
				if (compBmodule.getCalls() != null) {
					for (int callee : compBmodule.getCalls()) {
						if (compAmodule.getId() == callee) {
							if (!result.contains(compAmodule)) {
								result.add(compAmodule);
								// collect caller too
								for (Integer compId : getCaller(compList, callee)) {
									if (!result.contains(compList.get(compId))) {
										result.add(compList.get(compId));
									}
								}
							}
							if (!result.contains(compBmodule)) {
								result.add(compBmodule);
								// collect caller too
								for (Integer compId : getCaller(compList, compBmodule.getId())) {
									// System.out.println("founding caller "+ compId +"
									// numOfComponent:"+numOfComponent);
									if (!result.contains(compList.get(compId))) {
										result.add(compList.get(compId));
									}
								}
							}
						}
					}
				}

			}
		}
		List<ComponentConfig> calleeList = new ArrayList<ComponentConfig>();
		if (result.size() < MAX_COMPONENT) {
			for (ComponentConfig comp : result) {
				if (comp.getCalls() != null) {
					for (Integer callee : comp.getCalls()) {
						if (!result.contains(compList.get(callee)) && !calleeList.contains(compList.get(callee)))
							calleeList.add(compList.get(callee));
					}
				}
			}
		}
		result.addAll(calleeList);

		// clone object to prevent cleaning up
		List<ComponentConfig> cloneList = new ArrayList<ComponentConfig>();
		for (ComponentConfig comp : result) {
			cloneList.add(comp.clone());
		}

		return cloneList;
	}

	private static List<SubModel> overlapModel(Hashtable<Integer, ComponentConfig> compList, List<SubModel> orModel) {
		List<SubModel> overlapList = new ArrayList<SubModel>();
		RandomUtil rand = new RandomUtil();
		int modelid = rand.randomNumber(100, 999);
		for (int i = 0; i < orModel.size(); i++) {
			for (int j = 0; j < orModel.size(); j++) {
				if (i != j) {
					// match calls array with id in the module list by j
					List<ComponentConfig> ol;
					try {
						ol = overlapComp(compList, orModel.get(i).getComponents(), orModel.get(j).getComponents());
						if (ol.size() > 0) {
							SubModel mod = new SubModel(modelid++, ol);
							// mod.clean();
							overlapList.add(mod);
						}
					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}
		return overlapList;
	}

	private static List<ComponentConfig> collectComp(Hashtable<Integer, ComponentConfig> compList,
			List<ComponentConfig> result, int startId, int numOfComponent) {
		ComponentConfig comp = compList.get(startId);
		if (comp != null && !result.contains(comp) && result.size() < MAX_COMPONENT) {
			result.add(comp);
			numOfComponent--;

			// collect component that it calls to
			if (comp != null) {
				if (comp.getCalls() != null) {

					for (int i = 0; i < comp.getCalls().length; i++) {
						if (numOfComponent > 0) {
							System.out.println("adding " + comp.getCalls()[i] + " numOfComponent " + numOfComponent);
							List<ComponentConfig> resub = collectComp(compList, result, comp.getCalls()[i],
									numOfComponent);
							for (ComponentConfig sub : resub) {
								if (sub != null && !result.contains(sub) && result.size() < MAX_COMPONENT) {
									result.add(sub);
									numOfComponent--;
								}
							}
						}
					}
				}

				// collect caller to this component
				for (Integer compId : getCaller(compList, comp.getId())) {
					System.out.println("founding caller " + compId + " numOfComponent:" + numOfComponent);
					if (!result.contains(compList.get(compId)) && result.size() < MAX_COMPONENT) {
						result.add(compList.get(compId));
						numOfComponent--;
					}
				}
			}
		}

		return result;
	}

	private static void printModels(List<SubModel> modelList) {
		System.out.println("=================================================");
		for (SubModel mod : modelList) {
			System.out.println("========= Model " + mod.getId() + " =========");
			for (ComponentConfig comp : mod.getComponents()) {
				System.out.print("Id: " + comp.getId() + (comp.isIsstartcaller() ? "*" : "") + " calls: [");
				if (comp.getCalls() != null)
					for (int j = 0; j < comp.getCalls().length; j++)
						System.out.print(comp.getCalls()[j] + ((j == comp.getCalls().length - 1) ? "" : ","));
				System.out.println("]");
			}
		}
	}

	private static void printModelsJson(List<SubModel> modelList) {
		Gson gson = new GsonBuilder().create();
		for (SubModel mod : modelList) {
			System.out.println("========= Model " + mod.getId() + " =========");
			System.out.println(gson.toJson(mod.getComponents()));
		}
	}

	private static void writeModels(List<SubModel> modelList, String modelFile) {
		Gson gson = new GsonBuilder().create();
		BufferedWriter writer = null;
		try {
			File targetFile = new File(modelFile);
			File parent = targetFile.getParentFile();
			if (!parent.exists() && !parent.mkdirs()) {
			    throw new IllegalStateException("Couldn't create dir: " + parent);
			}
			
			writer = new BufferedWriter(new FileWriter(modelFile, false));
			for (SubModel mod : modelList) {
				writer.write(gson.toJson(mod.getComponents()) + "\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	private static List<Integer> getCaller(Hashtable<Integer, ComponentConfig> compList, int id) {
		Set<Integer> keys = compList.keySet();
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (Integer key : keys) {
			if (compList.get(key).getCalls() != null) {
				for (int eachCall : compList.get(key).getCalls()) {
					// System.out.println(" match caller"+key + "=="+id);
					if (eachCall == id) {
						result.add(key);
						break;
					}
				}
			}
		}

		return result;
	}

}
