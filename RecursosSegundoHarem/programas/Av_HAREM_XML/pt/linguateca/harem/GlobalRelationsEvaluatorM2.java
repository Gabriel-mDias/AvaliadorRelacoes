package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import pt.linguateca.relations.RelationEvaluation;
import pt.linguateca.relations.RelationsInDocEvaluation;

/**
 * Gerador resultados ReRelEM
 * @author Besugo
 *
 */
public class GlobalRelationsEvaluatorM2 extends GlobalEvaluator{

	public static final String HEADER = "Avalia\u00e7\u00e3o do ReRelEM";
	public static final String FILTER1 = "Descri\u00e7\u00e3o do cen\u00e1rio HAREM";
	public static final String FILTER2 = "Descri\u00e7\u00e3o do cen\u00e1rio ReRelEM";

	public static final String CORELS_HEADER = "Avalia\u00e7\u00e3o de CORELs";
	public static final String RELATIONS_HEADER = "Avalia\u00e7\u00e3o de Rela\u00e7\u00f5es";

	public static final String SCORE_HEADER = "Avalia\u00e7\u00e3o Global - ReRelEM";
	public static final String SCORE_IN_GC = "Pontua\u00e7\u00e3o na CD";
	public static final String MAXIMUM_SYSTEM_SCORE = "Pontua\u00e7\u00e3o m\u00e1xima poss\u00edvel do Sistema";
	public static final String SYSTEM_SCORE = "Pontua\u00e7\u00e3o do Sistema";

	public static final String PRECISION = "Precis\u00e3o";
	public static final String RECALL = "Abrang\u00eancia";
	public static final String F_MEASURE = "Medida F";

	public GlobalRelationsEvaluatorM2(String alignments, boolean useTags, boolean debug)
	{
		super(alignments, useTags, debug);
		new Thread(this).start();
	}

	@Override
	public void run() {

		BufferedReader reader = null;
		String buffer;
		int state = -1;

		ValuesParser parser = new ValuesParser();
		String currentEvaluation;
		double currentScore;

		double maximumScoreInGc = 0.0;
		double maximumScoreInPart = 0.0;

		double totalScore = 0.0;

		int totalCorelsInGC = 0;
		int totalCorelsInPart = 0;

		int totalCorrectCorels = 0;
		int totalSpuriousCorels = 0;
		int totalMissingCorels = 0;

		int totalRelationsInGC = 0;
		int totalRelationsInPart = 0;

		int totalCorrectRelations = 0;
		int totalSpuriousRelations = 0;
		int totalMissingRelations = 0;	

		int currentRelationsInGC = 0;
		int currentRelationsInPart = 0;
		int currentCorelsInPart = 0;
		int currentCorrectCorels = 0;

		try
		{
			reader = new BufferedReader(new FileReader(_alignmentsFile));

			//filtro
			System.out.println(HEADER);
			System.out.println(FILTER1);
			System.out.println("\t"+reader.readLine());
			System.out.println(FILTER2);
			System.out.println("\t"+reader.readLine());
			
			System.out.println();
	
			while ((buffer = reader.readLine()) != null)
			{
				if(buffer.startsWith(_tagBase.getDocTag())){

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
					//parsing da linha
					parser.setBuffer(buffer);

					currentRelationsInGC = parser.getInt(RelationsInDocEvaluation.RELATIONS_GC);
					currentRelationsInPart = parser.getInt(RelationsInDocEvaluation.RELATIONS_PART);
					currentCorrectCorels = parser.getInt(RelationsInDocEvaluation.CORRECT_CORELS);
					
					totalRelationsInGC += currentRelationsInGC;
					totalRelationsInPart += currentRelationsInPart;
					totalCorrectCorels += currentCorrectCorels;
					
					totalCorelsInGC += parser.getInt(RelationsInDocEvaluation.UNIQUE_ARG_PAIRS_GC);
					currentCorelsInPart = parser.getInt(RelationsInDocEvaluation.UNIQUE_ARG_PAIRS_PART);			
					totalCorelsInPart += currentCorelsInPart;
					
					//pontuações máximas
					maximumScoreInGc += RelationEvaluation.getScoreForCorrectRelation() * currentRelationsInGC;
					maximumScoreInPart += RelationEvaluation.getScoreForCorrectRelation() * currentRelationsInPart
						+ RelationEvaluation.getScoreForCorrectCorel() * (currentCorelsInPart - currentRelationsInPart);
					
					state = 1;
					continue;
				}

				if(state == 1)
				{					
					//parsing da linha a partir de ::
					parser.setBuffer(buffer.split(RelationEvaluation.EVALUATION_MARKER)[1].trim());		
										
					currentEvaluation = parser.getString(RelationEvaluation.EVALUATION_STRING);
					
					currentScore = getScore(currentEvaluation);
					totalScore += currentScore;
					
					if(currentEvaluation.equals(RelationEvaluation.RELATION_CORRECT_STRING))
					{
						totalCorrectRelations++;
					}
					else if(currentEvaluation.equals(RelationEvaluation.COREL_CORRECT_STRING))
					{
						totalSpuriousRelations++;
					}
					else if(currentEvaluation.equals(RelationEvaluation.SPURIOUS_RELATION_STRING))
					{
						totalSpuriousRelations++;
					}
					else if(currentEvaluation.equals(RelationEvaluation.MISSING_RELATION_STRING))
					{
						totalMissingRelations++;
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

		totalSpuriousCorels = (totalCorelsInPart - totalCorrectCorels);
		totalMissingCorels = (totalCorelsInGC - totalCorrectCorels);


		double precision = (double) totalCorrectCorels / (double) totalCorelsInPart;
		double recall = (double) totalCorrectCorels / (double) totalCorelsInGC;
		double f = (2 * precision * recall) / (precision + recall);

		System.out.println(CORELS_HEADER);
		System.out.println(TOTAL_IN_GC + ": " + totalCorelsInGC);
		System.out.println(TOTAL_IDENTIFIED + ": " + totalCorelsInPart);
		System.out.println(TOTAL_CORRECT_M + ": " + totalCorrectCorels);
		System.out.println(SPURIOUS + ": " + totalSpuriousCorels);
		System.out.println(MISSING + ": " + totalMissingCorels);
		System.out.println(PRECISION + ": " + precision);
		System.out.println(RECALL + ": " + recall);
		System.out.println(F_MEASURE + ": " + f);


		precision = (double) totalCorrectRelations / (double) totalRelationsInPart;
		recall = (double) totalCorrectRelations / (double) totalRelationsInGC;
		f = (2 * precision * recall) / (precision + recall);

		System.out.println("\n"+RELATIONS_HEADER);
		System.out.println(TOTAL_IN_GC + ": " + totalRelationsInGC);
		System.out.println(TOTAL_IDENTIFIED + ": " + totalRelationsInPart);
		System.out.println(TOTAL_CORRECT_F + ": " + totalCorrectRelations);
		System.out.println(SPURIOUS + ": " + totalSpuriousRelations);
		System.out.println(MISSING + ": " + totalMissingRelations);
		System.out.println(PRECISION + ": " + precision);
		System.out.println(RECALL + ": " + recall);
		System.out.println(F_MEASURE + ": " + f);


		precision = totalScore / maximumScoreInPart;
		recall = totalScore / maximumScoreInGc;
		f = (2 * precision * recall) / (precision + recall);

		System.out.println("\n"+SCORE_HEADER);
		System.out.println(SCORE_IN_GC + ": " + maximumScoreInGc);
		System.out.println(MAXIMUM_SYSTEM_SCORE + ": " + maximumScoreInPart);
		System.out.println(SYSTEM_SCORE + ": " + totalScore);
		System.out.println(PRECISION + ": " + precision);
		System.out.println(RECALL + ": " + recall);
		System.out.println(F_MEASURE + ": " + f);

		try
		{
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private double getScore(String evaluation)
	{
		if(evaluation == null)
			return 0;
		
		if(evaluation.equals(RelationEvaluation.RELATION_CORRECT_STRING))
			return RelationEvaluation.DEFAULT_COREL_CORRECT + RelationEvaluation.DEFAULT_TIPOREL_CORRECT;
		else if(evaluation.equals(RelationEvaluation.COREL_CORRECT_STRING))
			return RelationEvaluation.DEFAULT_COREL_CORRECT;
		else if(evaluation.equals(RelationEvaluation.SPURIOUS_RELATION_STRING))
			return -RelationEvaluation.DEFAULT_COREL_SPURIOUS;
		else return 0;
	}
	
	public static void main(String args[])
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

			if (args[i].equals("-depurar") || args[i].equals("-debug"))
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

		new GlobalRelationsEvaluatorM2(alignments, useTags, debug);
	}

	private static void printSynopsis()
	{
		System.out.println("Utiliza\u00e7\u00e3o:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.GlobalRelationsEvaluator -alinhamento <ficheiro_alinhamentos_corels_avaliados>");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.GlobalRelationsEvaluator -alinhamento participacao.alinhado.avalida.veu.alts.emir.expandido.normalizado.avaliado");
	}
}
