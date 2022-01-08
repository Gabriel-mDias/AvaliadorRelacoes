package pt.linguateca.harem.reports;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import org.jdom.Element;

import pt.linguateca.harem.ALTEntity;
import pt.linguateca.harem.DocumentReader;
import pt.linguateca.harem.HaremEntity;
import pt.linguateca.harem.NamedEntity;
import pt.linguateca.harem.NamedEntityTagParser;

/**
 * Relatorio dos tipos de EMs presentes num ficheiro
 * Tem como entrada uma CD
 * @author bESUGO
 *
 */
public class EntitiesReport {

	private final static String INDEF = "INDEFINIDO";
	private final static String TOTAL = "TOTAL";

	private DocumentReader _reader;
	private TreeMap<String, TreeMap<String, TreeMap<String, Integer>>> _table;
	private HashMap<Integer, LinkedList<ALTEntity>> _alts;

	public EntitiesReport(String file){

		_reader = new DocumentReader(file);
		_table = new TreeMap<String, TreeMap<String,TreeMap<String,Integer>>>();
		_alts = new HashMap<Integer, LinkedList<ALTEntity>>();
		
		report();
	}

	private void report(){

		LinkedList<String> cats, types, subs;
		HaremEntity entity;

		NamedEntityTagParser parser = new NamedEntityTagParser();
		Iterator it = _reader.getIterator();

		while(it.hasNext()){

			parser.setDocument((Element)it.next());
			while(parser.recognize()){

				entity = parser.getEntity();

				if(entity instanceof NamedEntity){
					NamedEntity ne = (NamedEntity)entity;
					//System.out.println(ne);

					cats = ne.getCategories();
					types = ne.getTypes();
					subs = ne.getSubtypes();			
					check(cats, types, subs);

				} else if(entity instanceof ALTEntity){
					ALTEntity alt = (ALTEntity)entity;
					
					updateAlts(alt);
					
					LinkedList<NamedEntity> entities = alt.getAllEntities();

					Iterator<NamedEntity> it_entities = entities.iterator();
					while(it_entities.hasNext()){

						NamedEntity ne = it_entities.next();
						//System.out.println(ne);

						cats = ne.getCategories();
						types = ne.getTypes();
						subs = ne.getSubtypes();			
						check(cats, types, subs);

					}					
				}

				//System.out.println(_table);
			}
		}
	}
	
	private void updateAlts(ALTEntity alt)
	{
		int n = alt.getAlternatives().size();
		if(!_alts.containsKey(n))
			_alts.put(n, new LinkedList<ALTEntity>());
		
		_alts.get(n).add(alt);
	}

	private void check(LinkedList<String> cats, LinkedList<String> types, LinkedList<String> subs){

		/*		System.out.println(cats);
		System.out.println(types);
		System.out.println(subs);*/

		for(int i = 0; i < cats.size(); i++){

			//TODO: verificar casos "|COISA"
			String cat = (cats.size() > i ? cats.get(i) : INDEF);
			String type = (types.size() > i ? types.get(i) : INDEF);
			String sub = (subs.size() > i ? subs.get(i) : INDEF);

			if(cat.equals(""))
				cat = INDEF;			
			if(type.equals(""))
				type = INDEF;
			if(sub.equals(""))
				sub = INDEF;

			if(!_table.containsKey(cat))
				_table.put(cat, new TreeMap<String, TreeMap<String,Integer>>());

			TreeMap<String, TreeMap<String,Integer>> typesMap = _table.get(cat);
			if(!typesMap.containsKey(type))
				typesMap.put(type, new TreeMap<String,Integer>());

			TreeMap<String,Integer> subsMap = typesMap.get(type);
			if(!subsMap.containsKey(sub))
				subsMap.put(sub, 1);
			else {
				int n = subsMap.get(sub);
				subsMap.put(sub, ++n);
			}
		}
	}

	public String toString(){		
		
		Iterator<String> it_cat, it_type, it_sub;
		String cat, type, sub;
		int totalCat = 0, totalType = 0;

		String ret = "ALTs:";
		
		for(Integer i : _alts.keySet())
			ret += "\n" + i + " alternativas = " + _alts.get(i).size();
		
		String catInfo = "", typeInfo = "", subInfo = "";

		it_cat = _table.keySet().iterator();
		while(it_cat.hasNext()){

			totalCat = 0;
			cat = it_cat.next();			
			catInfo = "\n" + cat;
			typeInfo = "";

			TreeMap<String, TreeMap<String,Integer>> typesMap = _table.get(cat);
			it_type = typesMap.keySet().iterator();
			while(it_type.hasNext()){

				totalType = 0;
				type = it_type.next();
				typeInfo += "\n\t" + type;
				subInfo = "";

				TreeMap<String,Integer> subsMap = typesMap.get(type);
				it_sub = subsMap.keySet().iterator();
				while(it_sub.hasNext()){

					sub = it_sub.next();

					int totalSub = subsMap.get(sub);
					totalCat += totalSub;
					totalType += totalSub;

					subInfo = "\n\t\t" + sub + " = " + totalSub;
				}

				typeInfo += " = " + totalType;
				typeInfo += subInfo;  
			}

			catInfo += " = " + totalCat;
			catInfo += typeInfo;
			ret += catInfo;
		}

		return ret;
	}

	/*	public String toString(){

		Iterator<String> it_cat, it_type, it_sub;
		String cat, type, sub;
		int totalCat = 0, totalType = 0;

		String ret = new String();

		it_cat = _table.keySet().iterator();
		while(it_cat.hasNext()){

			cat = it_cat.next();
			ret += "\n" + cat;

			TreeMap<String, TreeMap<String,Integer>> typesMap = _table.get(cat);
			it_type = typesMap.keySet().iterator();
			while(it_type.hasNext()){

				type = it_type.next();
				ret += "\n\t" + type;

				TreeMap<String,Integer> subsMap = typesMap.get(type);
				it_sub = subsMap.keySet().iterator();
				while(it_sub.hasNext()){

					sub = it_sub.next();

					int totalSub = subsMap.get(sub);
					totalCat += totalSub;
					totalType += totalSub;

					ret += "\n\t\t" + sub + " = " + totalSub;
				}
				ret += "\n\t\t" + TOTAL + " = " + totalType;  
			}
			ret += "\n\t" + TOTAL + " = " + totalCat;
		}

		//String ret = _table.toString();
		//ret = ret.replaceAll("=\\{", "\n\t");

		return ret;
	}*/

	public static void main(String args[]){

		System.out.println(new EntitiesReport(args[0]));

	}


}
