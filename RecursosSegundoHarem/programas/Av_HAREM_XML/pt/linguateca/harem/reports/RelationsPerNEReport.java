package pt.linguateca.harem.reports;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeMap;

import pt.linguateca.harem.AlignmentsToTriples;
import pt.linguateca.harem.RelationExpanderM2;
import pt.linguateca.harem.TagBase;
import pt.linguateca.relations.Relation;
import pt.linguateca.relations.RelationM2;
import pt.linguateca.relations.RelationProcessor;

/**
 * Todas as EM na CD e número de relações para cada
 * Entrada: triplas saídas do expandidor
 * @author Besugo
 *
 */
public class RelationsPerNEReport implements Runnable{

	private String _file;

	public RelationsPerNEReport(String file){

		this._file = file;

		new Thread(this).start();
	}

	public void run()
	{	
		TagBase tagBase = TagBase.getInstance();

		BufferedReader reader = null;
		String buffer;
		int state = -1;

		RelationProcessor processor = new RelationProcessor();
		RelationM2 current = null;
		Relation simpleCurrent = null;
		LinkedHashMap<String, Set<Relation>> entityRelationsMap = null;
		LinkedHashMap<String, TreeMap<String, Integer>> relationCategoriesMap = null;
		Set<String> facetsSet = null;
		
		LinkedHashMap<String, TreeMap<String, Integer>> totalRelationCategoriesMap =
			new LinkedHashMap<String, TreeMap<String,Integer>>();
		int totalFacets = 0;
		int totalEMs = 0;
		
		try
		{
			reader = new BufferedReader(new FileReader(_file));

			while ((buffer = reader.readLine()) != null)
			{
				//filtros
				if(buffer.startsWith("#")){
					continue;
				}		

				if(buffer.startsWith(tagBase.getDocTag())){
					System.out.println("\n"+buffer);
					state = 0;

					entityRelationsMap = new LinkedHashMap<String, Set<Relation>>();
					facetsSet = new HashSet<String>();
					relationCategoriesMap = new LinkedHashMap<String, TreeMap<String,Integer>>();
					continue;
				}

				else if(buffer.startsWith(tagBase.getEndOfDocTag()))
				{
					printDocInfo(entityRelationsMap, facetsSet, relationCategoriesMap);
					
					totalEMs += entityRelationsMap.size();
					totalFacets += facetsSet.size();
					
					System.out.println("ANTES: "+totalRelationCategoriesMap);
					totalRelationCategoriesMap = merge(totalRelationCategoriesMap, relationCategoriesMap);
					System.out.println("DEPOIS: "+totalRelationCategoriesMap);
					
					System.out.println(buffer);
					state = -1;

					continue;
				}

				else if ((state == 0 || state == 1) && buffer.startsWith(AlignmentsToTriples.GC))
				{
					state = 1;
					continue;
				}

				else if ((state == 0 || state == 1) && buffer.startsWith(AlignmentsToTriples.PARTICIPATION))
				{
					state = 2;
					continue;
				}

				else if (buffer.trim().equals(""))
				{
					continue;
				}

				current = processor.getRelationM2(buffer);
				simpleCurrent = current.getSimpleRelationWithSuperID();
				
				if(state == 1)
				{
					putInEntityRelationsMap(entityRelationsMap, current);
					putInRelationCategoriesMap(relationCategoriesMap, current);
					putInSet(facetsSet, simpleCurrent.getA());
					putInSet(facetsSet, simpleCurrent.getB());
				}

				if(state == 2)
				{
					continue;
				}
			}
		}

		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("TOTAL");
		System.out.println("EMs = "+totalEMs);
		System.out.println("Facetas = "+totalFacets);
		printRelationCategoriesMap(totalRelationCategoriesMap);
		
		try
		{
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void putInEntityRelationsMap(LinkedHashMap<String, Set<Relation>> map, RelationM2 relation)
	{
		 if(!map.containsKey(relation.getA()))
			 map.put(relation.getA(), new HashSet<Relation>());
			 
		 map.get(relation.getA()).add(relation);
	}
	
	private void putInRelationCategoriesMap(LinkedHashMap<String, TreeMap<String, Integer>> map, RelationM2 relation)
	{
		 if(!map.containsKey(relation.getType()))
			 map.put(relation.getType(), new TreeMap<String, Integer>());
			 
		 if(!map.get(relation.getType()).containsKey(relation.getSourceCategory()))
			 map.get(relation.getType()).put(relation.getSourceCategory(), 0);
		 
		 if(!map.get(relation.getType()).containsKey(relation.getTargetCategory()))
			 map.get(relation.getType()).put(relation.getTargetCategory(), 0);
		 
		 int inc = map.get(relation.getType()).get(relation.getSourceCategory()) + 1;
		 map.get(relation.getType()).put(relation.getSourceCategory(), inc);
		 
		 inc = map.get(relation.getType()).get(relation.getTargetCategory()) + 1;
		 map.get(relation.getType()).put(relation.getTargetCategory(), inc);
	}
	
	private LinkedHashMap<String, TreeMap<String, Integer>> merge(
			LinkedHashMap<String, TreeMap<String, Integer>> relationCategories1,
			LinkedHashMap<String, TreeMap<String, Integer>> relationCategories2)
	{
		LinkedHashMap<String, TreeMap<String, Integer>> toReturn = relationCategories1;
		int sum;
		
		for(String r : relationCategories2.keySet())
		{
			if(!toReturn.containsKey(r))
				 toReturn.put(r, new TreeMap<String, Integer>());
			
			for(String c : relationCategories2.get(r).keySet())
			{
				if(!toReturn.get(r).containsKey(c))
					 toReturn.get(r).put(c, relationCategories2.get(r).get(c));
				else
				{
					sum = toReturn.get(r).get(c) + relationCategories2.get(r).get(c);
					toReturn.get(r).put(c, sum);
				}
			}
		}
		
		return toReturn;
	}
	
	private void putInSet(Set<String> set, String idAndFacet)
	{
		set.add(idAndFacet);
	}
	
	private void printDocInfo(LinkedHashMap<String, Set<Relation>> relationEntities,
			Set<String> set, LinkedHashMap<String, TreeMap<String, Integer>> relationCategories)
	{
		System.out.println("EMs\t"+relationEntities.size());
		System.out.println("Facetas\t"+set.size());
		
		for(String s : relationEntities.keySet())
		{
			System.out.println("\t"+s+"\t"+relationEntities.get(s).size());
		}
		
		printRelationCategoriesMap(relationCategories);
	}
	
	private void printRelationCategoriesMap(LinkedHashMap<String, TreeMap<String, Integer>> relationCategories)
	{
		System.out.println("Categorias em relacoes:");
		for(String r : relationCategories.keySet())
		{
			System.out.println("\t\t"+r);
			for(String c : relationCategories.get(r).keySet())
			{
				System.out.println("\t\t\t"+c+" = "+relationCategories.get(r).get(c));
			}	
		}
	}

	public static void main(String args[]){

		String file = null;

		try{
			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equals("-entrada"))
				{
					i++;
					file = args[i];
					continue;
				}
			}

		} catch (Exception e)
		{
			printSynopsis();
			return;
		}

/*		try {
			System.setOut(new PrintStream("relacoes_por_em.csv"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		new RelationsPerNEReport(file);
	}

	/**
	 * No caso de os argumentos de entrada nao serem correctamente fornecidos.
	 * Este metodo imprime uma mensagem de ajuda para a consola.
	 * 
	 */
	private static void printSynopsis()
	{
		System.out.println("Utiliza\u00e7\u00e3o:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 cp .;lib/jdom.jar pt.linguateca.harem.RelationsEvaluatorM2 -alinhamento <ficheiro_relacoes>");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 cp .;lib/jdom.jar pt.linguateca.harem.RelationsEvaluatorM2 -alinhamento ficheiro.alinhado.alts.expandido");
	}
}
