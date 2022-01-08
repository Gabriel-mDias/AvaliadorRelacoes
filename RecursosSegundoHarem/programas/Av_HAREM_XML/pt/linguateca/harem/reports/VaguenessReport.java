package pt.linguateca.harem.reports;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import pt.linguateca.harem.ALTEntity;
import pt.linguateca.harem.DocumentReader;
import pt.linguateca.harem.HaremEntity;
import pt.linguateca.harem.NamedEntity;
import pt.linguateca.harem.NamedEntityTagParser;
import pt.linguateca.harem.TaggedDocument;

/**
 * Relatorio de vagueza
 * Tem como entrada uma CD
 * @author bESUGO
 *
 */
public class VaguenessReport {

	private DocumentReader _reader;
	private LinkedHashMap<String, VaguenessInDoc> _table;

	private VaguenessInDoc _totalInfo;

	public VaguenessReport(String file){

		_reader = new DocumentReader(file);
		_table = new LinkedHashMap<String, VaguenessInDoc>();

		_totalInfo = new VaguenessInDoc("TOTAL");
		_table.put("TOTAL", _totalInfo);

		report();
	}

	private void report(){

		HaremEntity entity;
		TaggedDocument currentDoc;
		VaguenessInDoc currentInfo;

		NamedEntityTagParser parser = new NamedEntityTagParser();

		while((currentDoc = _reader.getNextDocument()) != null){

			parser.setDocument(currentDoc.getDocument());
			currentInfo = new VaguenessInDoc("DOC "+currentDoc.getID());

			while(parser.recognize()){

				entity = parser.getEntity();

				if(entity instanceof NamedEntity){
					NamedEntity ne = (NamedEntity)entity;
					//System.out.println(ne);

					if(ne.isOmitted())
						continue;

					update(currentInfo, ne.getCategories());

				} else if(entity instanceof ALTEntity){
					ALTEntity alt = (ALTEntity)entity;
					LinkedList<NamedEntity> entities = alt.getAllEntities();

					Iterator<NamedEntity> it_entities = entities.iterator();
					while(it_entities.hasNext()){

						NamedEntity ne = it_entities.next();
						//System.out.println(ne);
						
						if(ne.isOmitted())
							continue;

						update(currentInfo, ne.getCategories());
					}					
				}
				_table.put(currentDoc.getID(), currentInfo);
				//System.out.println(_table);
			}
		}
	}

	private void update(VaguenessInDoc info, LinkedList<String> cats){

		info.addNamedEntity(cats);
		_totalInfo.addNamedEntity(cats);
	}

	public String toString(){

		String toReturn = "";

		for(String s : _table.keySet())
			toReturn += _table.get(s).toString()+"\n";

		return toReturn;
	}

	public static void main(String args[]){

		System.out.println(new VaguenessReport(args[0]));

	}

	class VaguenessInDoc
	{
		private String _docid;
		private TreeMap<Integer, Integer> _vagueness;
		private TreeMap<String, Integer> _categories;

		private int _totalNEs;
		private int _totalFacets;

		public VaguenessInDoc(String docid)
		{
			_docid = docid;
			_vagueness = new TreeMap<Integer, Integer>();
			_categories = new TreeMap<String, Integer>();

			_totalNEs = 0;
			_totalFacets = 0;
		}

		public void addNamedEntity(LinkedList<String> cats)
		{
			int numCats = cats.size();

			_totalNEs++;
			_totalFacets += (numCats > 0 ? numCats : 1);

			if(!_vagueness.containsKey(numCats))
			{
				_vagueness.put(numCats, 0);
			}

			int update = _vagueness.get(numCats) + 1;
			_vagueness.put(numCats, update);


			for(String s : cats)
			{
				if(!_categories.containsKey(s))
				{
					_categories.put(s, 0);
				}

				update = _categories.get(s) + 1;
				_categories.put(s, update);
			}
		}

		/*public void incVagueness(int index)
	{
		if(!_vagueness.containsKey(index))
		{
			_vagueness.put(index, 0);
		}

		int update = _vagueness.get(index) + 1;
		_vagueness.put(index, update);
	}

	public void incTotalFacets(int n)
	{
		_totalNEs++;
		_totalFacets += n;
	}*/

		public String toString()
		{
			String toReturn = _docid+"\n";

			toReturn += "\tTotal EMs = "+_totalNEs+"\n";
			toReturn += "\tTotal Facetas = "+_totalFacets;

			for(Integer i : _vagueness.keySet())
			{
				toReturn += "\n\t\tEMs com "+i+" categorias = "+_vagueness.get(i);
			}
			
			toReturn += "\n\tCategorias:";
			
			for(String s : _categories.keySet())
			{
				toReturn += "\n\t\t"+s+" = "+_categories.get(s);
			}

			return toReturn;
		}
	}
}
