package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * Gerador de resultados/avaliador global
 * Lê todas as saídas do Resumidor para gerar tabelas HTML com os resultados.
 * @author Besugo
 *
 */
public class GlobalSemanticEvaluator extends GlobalEvaluator
{
	public static final String FILTER = "Descri\u00e7\u00e3o do cen\u00e1rio avaliado";

	public static final String IDENTIFICATION_TOKEN = "Avalia\u00e7\u00e3o Global - Identifica\u00e7\u00e3o";

	public static final String COMBINED_TOKEN = "Avalia\u00e7\u00e3o Global - Classifica\u00e7\u00e3o";

	public static final String SEMANTIC_TOTAL_IN_GC = "Total de EMs identificadas na CD";

	public static final String SEMANTIC_TOTAL_IDENTIFIED = "Total de EMs identificadas pelo sistema";

	public static final String MAXIMUM_CLASSIFICATION_IN_GC = "Valor m\u00e1ximo poss\u00edvel para a Classifica\u00e7\u00e3o na CD";

	public static final String MAXIMUM_CLASSIFICATION_IN_SYSTEM = "Valor m\u00e1ximo poss\u00edvel para a Classifica\u00e7\u00e3o do sistema";

	public static final String SYSTEM_CLASSIFICATION = "Valor da Classifica\u00e7\u00e3o do sistema";

	public static final String MAXIMUM_PRECISION = "Precis\u00e3o M\u00e1xima do Sistema";

	public static final String MAXIMUM_RECALL = "Abrang\u00eancia M\u00e1xima na CD";

	private EntitiesAttributesFilter _filter;

	public GlobalSemanticEvaluator(String alignments, boolean useTags, boolean debug)
	{
		super(alignments, useTags, debug);
		new Thread(this).start();
	}

	public void run()
	{
		BufferedReader reader = null;
		String buffer;
		SemanticEvaluatedAlignment alignment;
		Iterator<NamedEntity> i;
		NamedEntity current;
		String filter = null;
		SemanticEvaluatedAlignmentProcessor processor = new SemanticEvaluatedAlignmentProcessor();

		SemanticScoreTuple scoreTuple = null;

		double currentMaximumCsc;
		double currentSystemCsc;

		double totalEntitiesInGoldenSet = 0;
		double totalEntitiesMissing = 0;
		double totalEntitiesSpurious = 0;
		double totalEntitiesIdentified = 0;

		double maximumCscInGc = 0;
		double maximumSystemCsc = 0;
		double totalSystemCsc = 0;
		double cscPrecision = 0;
		double cscRecall = 0;
		double identificationPrecision = 0;
		double identificationRecall = 0;

		double alt = 1.0;
		
		try
		{
			reader = new BufferedReader(new FileReader(_alignmentsFile));

			filter = reader.readLine();
			_filter = new EntitiesAttributesFilter(filter, _tagBase);

			while ((buffer = reader.readLine()) != null)
			{
				if (!isEvaluatable(buffer))
					continue;

				alignment = (SemanticEvaluatedAlignment) processor.getEvaluatedAlignment(buffer);
				currentMaximumCsc = 0;

				if (_debug)
				{
					System.out.println(alignment);
				}
				
				for (i = alignment.getAligned(); i.hasNext();)
				{							
					current = i.next();
					
					scoreTuple = new SemanticScoreParser(alignment.getScore(current)).getScoreTuple();
					alt = scoreTuple.getAltWeight();

					if(current == null)
					{
						totalEntitiesMissing += alt;
					}
					else
					{						
						totalEntitiesIdentified += alt;
						if(alignment.isSpurious())
						{
							totalEntitiesSpurious++;
						}
											
						currentSystemCsc =
							scoreTuple.getCombinedSemanticClassification() * scoreTuple.getWeight() * alt;
						totalSystemCsc += currentSystemCsc;

						//System.out.println(scoreTuple.getCombinedSemanticClassification() * scoreTuple.getWeight());
						
						maximumSystemCsc += scoreTuple.getMaximumCSCinSystem() * alt;
												
/*						if((int)totalEntitiesIdentified != (int)maximumSystemCsc)
						{
							System.err.println(alignment);
							System.err.println("s_csc= "+currentSystemCsc);
							System.err.println("total_s_csc= "+totalSystemCsc);
							System.err.println("ems= "+totalEntitiesIdentified);
							System.err.println("max_csc= "+maximumSystemCsc);
							//return;
						}*/
					}
				}
				
				if(_debug)
				{
					System.out.println("MaxCSC_CD="+scoreTuple.getMaximumCSCinGC());
					System.out.println("MaxCSC_S="+scoreTuple.getMaximumCSCinSystem());
				}
				
				if (!alignment.isSpurious())
				{					
					totalEntitiesInGoldenSet += alt;
					
					//currentMaximumCsc = alignment.getGoldenEntity().getMaximumCSC(_filter) * alt;
					currentMaximumCsc = scoreTuple.getMaximumCSCinGC() * alt;
					maximumCscInGc += currentMaximumCsc;
				}			
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		System.out.println(FILTER + ":");
		System.out.println("\t" + filter);
		System.out.println("");

		System.out.println(IDENTIFICATION_TOKEN);
		System.out.println(SEMANTIC_TOTAL_IN_GC + ": " + totalEntitiesInGoldenSet);
		//System.out.format(Locale.FRANCE, SEMANTIC_TOTAL_IN_GC + ": %f\n", totalEntitiesInGoldenSet);
		System.out.println(SEMANTIC_TOTAL_IDENTIFIED + ": " + totalEntitiesIdentified);
		System.out.println(TOTAL_CORRECT_F + ": " + (totalEntitiesIdentified-totalEntitiesSpurious));
		System.out.println(SPURIOUS + ": " + totalEntitiesSpurious);
		System.out.println(MISSING + ": " + totalEntitiesMissing);

		identificationPrecision = (totalEntitiesIdentified - totalEntitiesSpurious) / totalEntitiesIdentified;
		identificationRecall = (totalEntitiesIdentified - totalEntitiesSpurious) / totalEntitiesInGoldenSet;

		System.out.println(PRECISION + ": " + identificationPrecision);
		System.out.println(RECALL + ": " + identificationRecall);
		System.out.println(F_MEASURE + ": " + (2 * identificationPrecision * identificationRecall)
				/ (identificationPrecision + identificationRecall));
		System.out.println(OVER_GENERATION + ": " + totalEntitiesSpurious / totalEntitiesIdentified);
		System.out.println(UNDER_GENERATION + ": " + totalEntitiesMissing / totalEntitiesInGoldenSet);

		System.out.println("\n");
		System.out.println("\n");

		cscPrecision = totalSystemCsc / maximumSystemCsc;
		cscRecall = totalSystemCsc / maximumCscInGc;

		System.out.println(COMBINED_TOKEN);
		System.out.println(MAXIMUM_CLASSIFICATION_IN_GC + ": " + maximumCscInGc);
		System.out.println(MAXIMUM_CLASSIFICATION_IN_SYSTEM + ": " + maximumSystemCsc);
		System.out.println(SYSTEM_CLASSIFICATION + ": " + totalSystemCsc);
		System.out.println(MAXIMUM_PRECISION + ": " + cscPrecision);
		System.out.println(MAXIMUM_RECALL + ": " + cscRecall);
		System.out.println(F_MEASURE + ": " + (2 * cscPrecision * cscRecall) / (cscPrecision + cscRecall));

		System.out.println("\n");
		System.out.println("\n");

		try
		{
			reader.close();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}

	}

	/*protected boolean canBeDuplicate(EvaluatedAlignment previous, EvaluatedAlignment current)
	{
		NamedEntity last = previous.getLastAlignment();
		NamedEntity first = current.getFirstAlignment();
		SemanticScoreParser parser;

		parser = new SemanticScoreParser(previous.getScore(last));
		double lastWeight = parser.getScoreTuple().getWeight();

		parser = new SemanticScoreParser(current.getScore(first));
		double firstWeight = parser.getScoreTuple().getWeight();

		return (lastWeight > 0 && lastWeight < 1 && firstWeight > 0 && firstWeight < 1);

	}*/

	public int stepFunction(int value)
	{
		if (value > 0)
			return 1;

		return 0;
	}

	public static void main(String[] args)
	{
		String alignments = null;
		boolean useTags = false;
		boolean debug = false;

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

			if (args[i].equals("-depurar"))
			{
				debug = true;
				continue;
			}
		}

		if (alignments == null)
		{
			printSynopsis();
			return;
		}

		new GlobalSemanticEvaluator(alignments, useTags, debug);
	}

	private static void printSynopsis()
	{
		System.out.println("Utiliza\u00e7\u00e3o:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.GlobalSemanticEvaluator -alinhamento <ficheiro_alinhamentos_semantica_avaliada>");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.GlobalSemanticEvaluator -alinhamento participacao.alinhado.avalida.veu.alts.emir");
	}
}
