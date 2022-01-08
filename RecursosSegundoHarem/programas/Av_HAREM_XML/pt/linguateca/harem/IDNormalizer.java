package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import pt.linguateca.relations.RelatedEntity;

/** Avaliacao do ReRelEM_M2 - normalizacao de IDs **/
public class IDNormalizer extends HaremEvaluator implements Runnable{
	
	private final String ID_SEP = "-";
	private long spuriousCounter;
	private EvaluatedAlignmentProcessor _processor;

	public IDNormalizer(String alignmentFile, boolean useTags)
	{
		super(alignmentFile, useTags);
		spuriousCounter = 1;
		_processor = new IdentificationEvaluatedAlignmentProcessor();
		new Thread(this).start();
	}

	public void run()
	{
		HashMap<String, String[]> partToGC = new HashMap<String, String[]>();
		HashMap<String, String[]> gcToPart = new HashMap<String, String[]>();

		BufferedReader reader = null;
		String buffer;
		IdentificationEvaluatedAlignment current;
		LinkedList<IdentificationEvaluatedAlignment> alignments = null;
		EntitiesAttributesFilter categoriesFilter = null;

		try
		{
			reader = new BufferedReader(new FileReader(_alignmentsFile));
			buffer = reader.readLine();

			//filtro
			if(buffer.startsWith("#")){
				categoriesFilter = new EntitiesAttributesFilter(buffer, _tagBase);
				System.out.println(buffer);
			}

			while ((buffer = reader.readLine()) != null)
			{
				//DOC DOCID
				if(buffer.startsWith(_tagBase.getDocTag()))
				{					
					if(alignments != null)
						printAlignments(normalize(alignments, partToGC, gcToPart, categoriesFilter));

					alignments = new LinkedList<IdentificationEvaluatedAlignment>();
					partToGC = new HashMap<String, String[]>();
					gcToPart = new HashMap<String, String[]>();
					System.out.println("\n"+buffer);
					
					continue;
				}
				else if (!isEvaluatable(buffer))
				{
					//System.out.println(buffer);
					continue;
				}

				current = (IdentificationEvaluatedAlignment)_processor.getEvaluatedAlignment(buffer);
				alignments.add(current);
				putInMap(partToGC, current.getGoldenEntity(), current.getFirstAlignment());
				putInMap(gcToPart, current.getFirstAlignment(), current.getGoldenEntity());
			}
			
			printAlignments(normalize(alignments, partToGC, gcToPart, categoriesFilter));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try
		{
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void putInMap(HashMap<String, String[]> map, NamedEntity golden, NamedEntity other)
	{
		if(other == null)
			return;

		String[] idCat = new String[2];
		LinkedList<String> categories = other.getCategories();
		
		if(golden.isSpurious())
		{
			idCat[0] = getNextSpuriousId();
			
			idCat[1] = (categories.isEmpty() ?
					_tagBase.getSimpleEntityTag() : NamedEntity.vagueValuesToString(categories));
			map.put(other.getId(), idCat);
		}

		else
		{
			idCat[0] = golden.getId();
			
			idCat[1] = (categories.isEmpty() ?
					_tagBase.getSimpleEntityTag() : NamedEntity.vagueValuesToString(categories));
			map.put(other.getId(), idCat);
		}
	}

	private String getNextSpuriousId()
	{
		return _tagBase.getSpuriousTag()+ID_SEP+(spuriousCounter++);	
	}

	private LinkedList<IdentificationEvaluatedAlignment> normalize(
			LinkedList<IdentificationEvaluatedAlignment> alignments, HashMap<String, String[]> partToGC,
			HashMap<String, String[]> gcToPart, EntitiesAttributesFilter catFilter){

		RelatedEntity golden = null;
		RelatedEntity aligned = null;
		String oldId = null;
		
		for(IdentificationEvaluatedAlignment alignment : alignments)
		{
			if(!alignment.isSpurious())
			{
				golden = new RelatedEntity(alignment.getGoldenEntity(), false, catFilter);
				golden.normalizeRelationAttributes(gcToPart, false);
			}
			
			if(!alignment.isNullAligned())
			{
				aligned = new RelatedEntity(alignment.getFirstAlignment(), true, catFilter);
				oldId = aligned.getId();
				
				if(partToGC.containsKey(oldId))
					aligned.setId(partToGC.get(oldId)[0]);
				else
					continue;
				
				aligned.normalizeRelationAttributes(partToGC, true);
			}
		}

		return alignments;	
	}

	private void printAlignments(LinkedList<IdentificationEvaluatedAlignment> alignments)
	{
		for(IdentificationEvaluatedAlignment alignment : alignments)
			System.out.println(alignment);
		
		System.out.println(_tagBase.getEndOfDocTag());
	}

	public static boolean isSpuriousId(String id)
	{
		return id.startsWith(_tagBase.getSpuriousTag());
	}
		
	public static void main(String[] args)
	{
		String alignments = null;
		boolean useTags = false;

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-alinhamento"))
			{
				i++;
				alignments = args[i];
				continue;
			}

			if (args[i].equals("-etiquetas"))
			{
				i++;
				useTags = args[i].equalsIgnoreCase("sim");
				continue;
			}
		}

		if (alignments == null)
		{
			printSynopsis();
			return;
		}

		new IDNormalizer(alignments, useTags);
	}

	private static void printSynopsis()
	{
		System.out.println("Utilizacao:" +
				"\njava -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.IDNormalizer -alinhamento ficheiro.alinhado.alts");
	}
}
