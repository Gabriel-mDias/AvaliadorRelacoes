package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * @author nseco, Besugo
 * 
 * Avaliador de alinhamentos de acordo com a identificação.
 */
public class IndividualAlignmentEvaluator extends HaremEvaluator implements Runnable
{
	public static final String CORRECT = "Correcto";

	public static final String PARTIAL_CORRECT_EXCESS = "Parcialmente_Correcto_por_Excesso";

	public static final String PARTIAL_CORRECT_LACK = "Parcialmente_Correcto_por_Defeito";

	public static final String MISSING = "Em_Falta";

	public static final String SPURIOUS = "Espurio";

	public static final String EVALUATION_MARKER = "::";
	
	public static final String ALT_WEIGHT_MARKER = "^";

	public IndividualAlignmentEvaluator(String alignmentFile, boolean useTags)
	{
		super(alignmentFile, useTags);
		new Thread(this).start();
	}

	public void run()
	{
		BufferedReader reader = null;
		String buffer;
		LinkedHashSet<NamedEntity> alignments;
		ArrayList<String> evaluations;
		NamedEntity current;
		
		try
		{
			reader = new BufferedReader(new FileReader(_alignmentsFile));
			
			//nome dos ficheiros
			//System.out.println(reader.readLine());
			//System.out.println(reader.readLine());
			
			while ((buffer = reader.readLine()) != null)
			{
				//garante que não é o DOCID, ALT...
				if (!isEvaluatable(buffer))
				{
					System.out.println(buffer);
					continue;
				}

				String[] lados = buffer.split(Aligner.ALIGNMENT_CONNECTOR);
				if(lados.length != 2)
				{
					System.out.println(buffer);
					continue;
				}
				
				alignments = new LinkedHashSet<NamedEntity>();
				current = new NamedEntity(lados[0]);				
				alignments.addAll(NamedEntity.toNamedEntityList(lados[1]));

				if (alignments.isEmpty())
				{
					alignments.add(null);
					evaluations = new ArrayList<String>();
					evaluations.add(MISSING);
				}
				else if (current.isSpurious())
				{
					evaluations = new ArrayList<String>();
					evaluations.add(SPURIOUS);
				}
				else
				{
					evaluations = evaluate(current, alignments);
				}

				if (_useTags)
				{
					System.out.println(current.toString() + Aligner.ALIGNMENT_CONNECTOR + alignments.toString()
							+ EVALUATION_MARKER + evaluations.toString());
				}
				else
				{
					System.out.println(current.unmarkTokens().toString() + Aligner.ALIGNMENT_CONNECTOR
							+ clean(alignments).toString() + EVALUATION_MARKER + evaluations.toString());
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

	protected ArrayList<String> evaluate(NamedEntity key, LinkedHashSet alignments)
	{
		ArrayList<String> evaluations = new ArrayList<String>(alignments.size());
		//CounterTagParser parser = new CounterTagParser();
		NamedEntity current;
		// LinkedList keyTokens = key.split(parser);
		LinkedList keyTokens = key.getNormalizedAtoms();
		LinkedList currentTokens;
		double score;

		for (Iterator i = alignments.iterator(); i.hasNext();)
		{
			current = (NamedEntity) i.next();
			// currentTokens = current.split(parser);
			currentTokens = current.getNormalizedAtoms();
			
			if (currentTokens.size() == keyTokens.size() && currentTokens.containsAll(keyTokens))
			{
				evaluations.add(CORRECT);
				continue;
			}

			score = getScore(keyTokens, currentTokens);

			if (currentTokens.size() >= keyTokens.size())
			{
				evaluations.add(PARTIAL_CORRECT_EXCESS + "(" + score + "; " + (1 - score) + ")");
				continue;
			}

			if (currentTokens.size() < keyTokens.size())
			{
				evaluations.add(PARTIAL_CORRECT_LACK + "(" + score + "; " + (1 - score) + ")");
				continue;
			}

		}

		return evaluations;
	}

	private double getScore(LinkedList list1, LinkedList list2)
	{
		double intersection = getIntersectionSize(list1, list2);
		return (intersection / (list1.size() + list2.size() - intersection)) / 2;
	}

	private int getIntersectionSize(LinkedList list1, LinkedList list2)
	{
		LinkedList shorter;
		HashSet longer = new HashSet();
		;
		int intersection = 0;

		if (list1.size() < list2.size())
		{
			shorter = list1;
			longer.addAll(list2);
		}
		else
		{
			shorter = list2;
			longer.addAll(list1);
		}

		for (Iterator i = shorter.iterator(); i.hasNext();)
		{
			if (longer.contains(i.next()))
			{
				intersection++;
			}
		}

		return intersection;
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

		new IndividualAlignmentEvaluator(alignments, useTags);
	}

	private static void printSynopsis()
	{
		System.out.println("Utiliza\u00e7\u00e3o:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.IndividualAlignmentEvaluator -alinhamento <ficheiro_alinhamentos>");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out.println("java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.IndividualAlignmentEvaluator -alinhamento participacao.alinhado");
	}

}
