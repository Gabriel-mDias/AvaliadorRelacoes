package pt.linguateca.harem.reports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import pt.linguateca.harem.ALTEntity;
import pt.linguateca.harem.DocumentReader;
import pt.linguateca.harem.HaremEntity;
import pt.linguateca.harem.NamedEntity;
import pt.linguateca.harem.NamedEntityTagParser;
import pt.linguateca.harem.TagBase;
import pt.linguateca.harem.TaggedDocument;
import pt.linguateca.harem.ValuesParser;
import pt.linguateca.relations.Relation;
import pt.linguateca.relations.RelationEvaluation;
import pt.linguateca.relations.RelationM2;
import pt.linguateca.relations.RelationProcessor;

public class SpuriousRelations{

	private static final String DIR = "participacoes";
	private static final String EXT = "total.alts.exp.selec.norm.triplas.todas.aval";

	private String CD_FILE = "CDSegundoHAREM_ReRelEM.v11.xml";

	//por DOC, por EM, relacao
	private LinkedHashMap<String, LinkedHashMap<String, LinkedList<Relation>>> _relations;
	private HashMap<String, String> _idToEntityMap;
	
	private TagBase _tagBase;

	public SpuriousRelations()
	{
		_relations = new LinkedHashMap<String, LinkedHashMap<String,LinkedList<Relation>>>();
		
		createIdToEntityMap();
		_tagBase = TagBase.getInstance();
	}
	
	private void createIdToEntityMap()
	{
		_idToEntityMap = new HashMap<String, String>();
		
		DocumentReader reader = new DocumentReader(CD_FILE);
		HaremEntity entity;
		TaggedDocument currentDoc;

		NamedEntityTagParser parser = new NamedEntityTagParser();

		while((currentDoc = reader.getNextDocument()) != null){

			parser.setDocument(currentDoc.getDocument());	

			while(parser.recognize()){

				entity = parser.getEntity();

				if(entity instanceof NamedEntity){
					NamedEntity ne = (NamedEntity)entity;
					//System.out.println(ne);

					_idToEntityMap.put(ne.getId(), ne.getEntity());

				} else if(entity instanceof ALTEntity){
					ALTEntity alt = (ALTEntity)entity;
					LinkedList<NamedEntity> entities = alt.getAllEntities();

					Iterator<NamedEntity> it_entities = entities.iterator();
					while(it_entities.hasNext()){

						NamedEntity ne = it_entities.next();
						_idToEntityMap.put(ne.getId(), ne.getEntity());
					}					
				}
				//System.out.println(_table);
			}
		}
	}

	public void list()
	{
		LinkedList<File> files = getFiles(new File(DIR), new EvaluatedFilter(EXT));
		
		System.out.println(files);
		BufferedReader reader;
		String buffer;

		int state = -1;
		String docid = null;
		String[] split;
		Relation relation;
		RelationProcessor processor = new RelationProcessor(); 
		ValuesParser parser = new ValuesParser();

		for(File f : files)
		{
			try {
				reader = new BufferedReader(new FileReader(f));

				while((buffer = reader.readLine()) != null)
				{	
					//filtros
					if(buffer.startsWith("#")){
						//System.out.println(buffer);
						continue;
					}		

					if(buffer.startsWith(_tagBase.getDocTag())){
						
						docid = buffer.trim();
						state = 0;
						continue;
					}

					else if(buffer.startsWith(_tagBase.getEndOfDocTag()))
					{
						state = -1;
						continue;
					}

					else if (buffer.trim().equals(""))
					{
						continue;
					}

					else if(state == 0 && buffer.startsWith("["))
					{	
						state = 1;
						continue;
					}

					if(state == 1)
					{						
						split = buffer.split(RelationEvaluation.EVALUATION_MARKER);

						if(isSpurious(parser, split[1]))
						{
							relation = processor.getRelation(split[0]);
							updateRelations(docid, relation);
						}
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean isSpurious(ValuesParser parser, String buffer)
	{
		parser.setBuffer(buffer.trim());
		String eval = parser.getString(RelationEvaluation.EVALUATION_STRING);
		return (eval.equals(RelationEvaluation.SPURIOUS_RELATION_STRING)
				|| eval.equals(RelationEvaluation.COREL_CORRECT_STRING));
	}

	private LinkedList<File> getFiles(File dir, FileFilter filter)
	{
		LinkedList<File> filteredFiles = new LinkedList<File>();

		try {
			// determine all subdirectories
			File files[] = dir.listFiles();
			int fileLength = files.length;
			for (int i = 0; i < fileLength; i++)
			{
				if (files[i].isDirectory()) {
					filteredFiles.addAll(getFiles(files[i], filter));
				} else {
					if (filter.accept(files[i])) {
						filteredFiles.add(files[i]);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


		return filteredFiles;
	}

	private void updateRelations(String docid, Relation relation)
	{
		if(!_relations.containsKey(docid))
		{
			_relations.put(docid, new LinkedHashMap<String, LinkedList<Relation>>());
		}

		if(!_relations.get(docid).containsKey(relation.getA()))
		{
			_relations.get(docid).put(relation.getA(), new LinkedList<Relation>());
		}

		if(!_relations.get(docid).get(relation.getA()).contains(relation))
			_relations.get(docid).get(relation.getA()).add(relation);
	}

	public String toString()
	{
		String toString = "";

		for(String doc : _relations.keySet())
		{
			toString += doc+"\n";
			for(String ne : _relations.get(doc).keySet())
			{
				for(Relation r :  _relations.get(doc).get(ne))
				{
					//toString += getRelationWithEntityAndId(r) + "\n";
					toString += getRelationWithEntityInsteadOfId(r) + "\n";
				}
			}
			toString += "\n";
		}

		return toString;
	}

	private Relation getRelationWithEntityInsteadOfId(Relation relation)
	{
		RelationM2 rm2 = (RelationM2)relation;
		String entityA = "\""+_idToEntityMap.get(rm2.getA())+"\"";
		String entityB = "\""+_idToEntityMap.get(rm2.getB())+"\"";
				
		return new RelationM2(relation.getType(), entityA, entityB, rm2.getSourceCategory(), rm2.getTargetCategory());
	}
	
	private Relation getRelationWithEntityAndId(Relation relation)
	{
		RelationM2 rm2 = (RelationM2)relation;
		String entityA = "\""+_idToEntityMap.get(rm2.getA())+"\""+" "+rm2.getA();
		String entityB = "\""+_idToEntityMap.get(rm2.getB())+"\""+" "+rm2.getB();
				
		return new RelationM2(relation.getType(), entityA, entityB, rm2.getSourceCategory(), rm2.getTargetCategory());
	}
	
	public static void main(String args[])
	{
		SpuriousRelations spurious = new SpuriousRelations();
		spurious.list();
		
		try {
			PrintStream out = new PrintStream("rels_espurias.txt");
			out.println(spurious);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class EvaluatedFilter implements FileFilter
	{
		private String _filter;

		public EvaluatedFilter(String filter)
		{
			_filter = filter;
		}

		public boolean accept(File file)
		{
			return file.getName().endsWith(_filter);
		}
	}
}


