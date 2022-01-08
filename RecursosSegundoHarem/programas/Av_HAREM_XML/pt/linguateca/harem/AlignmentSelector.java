package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author Besugo
 * Avaliacao do ReRelEM - Selector de alinhamentos
 * 
 */
public class AlignmentSelector extends HaremEvaluator implements Runnable{

	private EvaluatedAlignmentProcessor _processor;

	public AlignmentSelector(String alignmentFile, boolean useTags)
	{
		super(alignmentFile, useTags);
		_processor = new IdentificationEvaluatedAlignmentProcessor();
		new Thread(this).start();
	}

	public void run()
	{
		BufferedReader reader = null;
		String buffer;
		IdentificationEvaluatedAlignment current;
		
		LinkedList<String> goldenCategories = null;
		LinkedList<String> otherCategories = null;
		LinkedList<String> categoriesIntersection = null;
		
		LinkedList<IdentificationEvaluatedAlignment> alignments = null;

		try
		{
			reader = new BufferedReader(new FileReader(_alignmentsFile));
			buffer = reader.readLine();

			//filtro
			if(buffer.startsWith("#")){
				System.out.println(buffer);
			}

			while ((buffer = reader.readLine()) != null)
			{
				//DOC DOCID
				if(buffer.startsWith(_tagBase.getDocTag()))
				{
					alignments = new LinkedList<IdentificationEvaluatedAlignment>();
					System.out.println("\n"+buffer);
					continue;
				}
				
				else if(buffer.startsWith(_tagBase.getEndOfDocTag()))
				{
					if(alignments != null)
						printAlignments(alignments);		
					
					System.out.println(_tagBase.getEndOfDocTag());
					continue;
				}
				
				else if (!isEvaluatable(buffer))
				{
					continue;
				}

				current = (IdentificationEvaluatedAlignment)_processor.getEvaluatedAlignment(buffer);
				//System.out.println("#"+buffer);
				
				if(current.isSpurious() || current.isNullAligned())
				{
					continue;
				}
				else
				{
					goldenCategories = current.getGoldenEntity().getCategories();
					otherCategories = current.getFirstAlignment().getCategories();
					
					if(goldenCategories.isEmpty() || otherCategories.isEmpty())
					{
						alignments.add(current);
					}
					else
					{
						categoriesIntersection = getIntersection(goldenCategories, otherCategories);
						if(!categoriesIntersection.isEmpty())
						{
							current.leaveOnlyCategories(categoriesIntersection);
							alignments.add(current);
						}
					}
				}
			}
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

	public static LinkedList<String> getIntersection(List<String> list1, List<String> list2)
	{
		//System.out.println(list1+" intersecta "+list2);
		LinkedList<String> intersection = new LinkedList<String>();

		if (list1 == null || list2 == null)
		{
			return intersection;
		}
		
		//se uma das listas tiver EM, a intersecção é a que não tem EM
		if(list1.contains(TagBase.getInstance().getSimpleEntityTag()))
		{
			if(!list2.isEmpty())
				return new LinkedList<String>(list2);
			
			intersection.add(TagBase.getInstance().getSimpleEntityTag());
			return intersection;
		}
		
		if(list2.contains(TagBase.getInstance().getSimpleEntityTag()))
		{
			if(!list1.isEmpty())
				return new LinkedList<String>(list1);
			
			intersection.add(TagBase.getInstance().getSimpleEntityTag());
			return intersection;
		}

		for (String s : list1)
		{
			if (list2.contains(s))
			{
				intersection.add(s);
			}
		}
		return intersection;	
	}

	private void printAlignments(LinkedList<IdentificationEvaluatedAlignment> alignments)
	{
		for(IdentificationEvaluatedAlignment alignment : alignments)
			System.out.println(alignment);
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

		new AlignmentSelector(alignments, useTags);
	}

	private static void printSynopsis()
	{
		System.out.println("Utilizacao:" +
		"\njava -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.AlignmentSelector -alinhamento ficheiro.alinhado.alts");
	}
}
