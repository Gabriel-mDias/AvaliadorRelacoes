package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author nseco, Besugo
 * 
 * Classe principal do Alinhador. Esta classe recebe os argumentos da Linha de Comandos e inicia o processo de
 * alinhamento tendo em conta esses argumentos.
 */

public class Aligner extends HaremConsoleWriter implements Runnable
{
	public static final String ALIGNMENT_CONNECTOR = " ---> ";

	private static final String ALIGNMENT_WARNING_MESSAGE = "Verifique o seguinte alinhamento: ";

	private static final String FAULTY_ATOMS_MESSAGE = "Os \u00e1tomos seguintes n\u00c3o foram correctamente identificados: ";

	private static final String MISSING_DOCUMENT_MESSAGE = "N\u00c3o consegui encontrar o documento na CD: ";

	private static final String IGNORING_DOCUMENT_MESSAGE = "Estou a ignorar o documento na submiss\u00c3o: ";

	private static final String SUBMSSION_MISSING_DOCUMENTS_MESSAGE = "A submiss\u00c3o n\u00c3o continha documentos da CD: ";

	private static final String NIL_ALIGNMENT = "NIL";

	private String _submission;

	private String _goldenSet;

	/**
	 * Construtor.
	 * 
	 * @param submission
	 *          O nome do ficheiro contendo a submissão a ser alinhada.
	 * @param goldenSet
	 *          O nome do ficheiro contendo a Colecção Dourada.
	 * @param useTags
	 *          Um booleano indicando se o output deve ou não conter etiquetas númericas utilizadas durante o
	 *          alinhamento.
	 */
	public Aligner(String submission, String goldenSet, boolean useTags)
	{
		super(useTags);

		_submission = submission;
		_goldenSet = goldenSet;
		_useTags = useTags;

		_tagBase = TagBase.getInstance();

		new Thread(this).start();
	}

	/**
	 * Método principal que é chamado quando esta thread entra em execução. Este método começa por criar um
	 * DocumentReader para ler os documentos da submissão. A colecção dourada é totalmente carregada para
	 * memória e colocada numa HashMap.
	 * 
	 * Sempre que um documento é lido da submissão o documento correspondente é obtido da HashMap e o
	 * alinhamento é efectuado.
	 */
	public void run()
	{		
		DocumentReader submissionReader = new DocumentReader(_submission);		
		TaggedDocument goldenSetDoc;
		TaggedDocument submissionDoc;
		AlignmentMap alignmentMap;
		HashMap<String, TaggedDocument> goldenDocs;
		Iterator i;
		HaremEntity key;
		LinkedHashSet value;

		goldenDocs = getNormalizedGoldenSet();

		while ((submissionDoc = submissionReader.getNextDocument()) != null)
		{
			//goldenSetDoc = goldenDocs.get(submissionDoc.getID());
			goldenSetDoc = goldenDocs.remove(submissionDoc.getID().trim());

			if (goldenSetDoc == null)
			{
				System.err.println(MISSING_DOCUMENT_MESSAGE + submissionDoc.getID());
				continue;
			}

			if (submissionDoc.getText() == null || submissionDoc.getText().trim().equals(""))
			{
				System.err.println(IGNORING_DOCUMENT_MESSAGE + submissionDoc.getID());
				continue;
			}

			//System.out.println(goldenSetDoc.getDocument()+" "+submissionDoc.getDocument());

			alignmentMap = goldenSetDoc.alignDocument(submissionDoc);

			System.out.println(_tagBase.getDocTag()+" "+alignmentMap.getDocID());
			/*			System.out.print(" ");
			System.out.print(alignmentMap.getOrigin());
			System.out.print(" ");
			System.out.println(alignmentMap.getGenre());*/

			if (alignmentMap.hasFaults())
			{
				System.out.print(_tagBase.openTag(_tagBase.getManualVerificationTag()));
				System.out.print(" ");
				for (i = alignmentMap.getFaultyAlignmentsIterator(); i.hasNext();)
				{
					key = (HaremEntity) i.next();
					value = (LinkedHashSet) alignmentMap.getFaultyTokens(key);
					System.out.print(FAULTY_ATOMS_MESSAGE + value + ".");
					System.out.print(" ");
					if (!_useTags)
					{
						System.out.print(ALIGNMENT_WARNING_MESSAGE + key.unmarkTokens() + ALIGNMENT_CONNECTOR + null);
					}
					else
					{
						System.out.print(ALIGNMENT_WARNING_MESSAGE + key + ALIGNMENT_CONNECTOR + null);
					}
					System.out.print(" ");
				}
				System.out.println(_tagBase.closeTag(_tagBase.getManualVerificationTag()));
			}

			//System.out.println("no. of alignments = "+alignmentMap.getAlignementsSize());

			for (i = alignmentMap.getAlignmentsIterator(); i.hasNext();)
			{
				key = (HaremEntity) i.next();
				value = (LinkedHashSet) alignmentMap.getAlignment(key);

				value = noOmittedEntities(value);

				if(!key.isOmitted() && !value.isEmpty()){
					printAlignment(key, noOmittedEntities(value), _useTags);
					System.out.print("\n");
				}					
			}

			System.out.println(_tagBase.getEndOfDocTag());
			System.out.println("\n");
			System.out.flush();
		}

		if(!goldenDocs.isEmpty()){
			System.err.println(SUBMSSION_MISSING_DOCUMENTS_MESSAGE);
			for(String id : goldenDocs.keySet())
				System.err.println(id);
		}
	}

	/** cria um mapa sem entradas omitidas **/
	private LinkedHashSet noOmittedEntities(LinkedHashSet entities)
	{
		LinkedHashSet clean = new LinkedHashSet();

		for(Object obj : entities)
		{
			if(obj instanceof HaremEntity)
			{
				if(!((HaremEntity)obj).isOmitted())
					clean.add(obj);
			}
			else
				clean.add(obj);
		}
		return clean;
	}

	/**
	 * Metodo que carrega os documentos da coleccao dourada para uma HashMap.
	 * As chaves da HashMap sao os identificadores dos documentos.
	 * 
	 * @return A HashMap contendo os documentos da coleccao dourada
	 */
	private HashMap<String, TaggedDocument> getNormalizedGoldenSet()
	{
		HashMap<String, TaggedDocument> golden = new HashMap<String, TaggedDocument>();
		DocumentReader goldenSetReader = new DocumentReader(_goldenSet);
		TaggedDocument goldenSetDoc;

		while ((goldenSetDoc = goldenSetReader.getNextDocument()) != null)
		{
			//goldenSetDoc.normalize();
			golden.put(goldenSetDoc.getID().trim(), goldenSetDoc);
		}

		return golden;
	}

	private void printAlignment(HaremEntity key, HashSet value, boolean tags)
	{
		if (key instanceof NamedEntity)
		{
			printAlignment((NamedEntity) key, value, tags);
		}
		else
		{
			printAlignment((ALTEntity) key, value, tags);
		}
	}

	private void printAlignment(NamedEntity key, HashSet value, boolean tags)
	{		
		if (!_useTags)
		{			
			System.out.print(key.unmarkTokens() + ALIGNMENT_CONNECTOR + clean(value));
		}
		else
		{
			System.out.print(key + ALIGNMENT_CONNECTOR + value);
		}
	}

	private void printAlignment(ALTEntity key, HashSet value, boolean tags)
	{
		Collection collectionToPrint;
		Iterator i, j, k;
		LinkedList alternative;
		LinkedHashSet comparable;
		Object current;
		NamedEntity clean;
		int counter = 0;

		System.out.println(key.getOpeningTag());

		for (i = key.getAlternativesIterator(); i.hasNext();)
		{
			counter++;

			System.out.println(_tagBase.openTag(_tagBase.getAltTag() + counter));

			alternative = (LinkedList) i.next();

			if (hasNamedEntity(alternative))
			{
				for (j = alternative.iterator(); j.hasNext();)
				{
					current = j.next();
					if (current instanceof NamedEntity)
					{
						if (value.contains(null))
						{
							if (_useTags)
							{
								System.out.println(current + ALIGNMENT_CONNECTOR + value);
							}
							else
							{
								System.out.println(((NamedEntity) current).unmarkTokens() + ALIGNMENT_CONNECTOR
										+ clean(value));
							}
							continue;
						}

						comparable = getComparableAlignments((NamedEntity) current, value);
						if (_useTags)
						{
							System.out.println(current + ALIGNMENT_CONNECTOR + comparable);
						}
						else
						{
							System.out.println(((NamedEntity) current).unmarkTokens() + ALIGNMENT_CONNECTOR
									+ clean(comparable));
						}
					}
				}
			}
			else
			{
				if (!value.contains(null))
				{
					for (k = value.iterator(); k.hasNext();)
					{
						current = (NamedEntity) k.next();
						NamedEntity spurious = new NamedEntity();
						spurious.setEntity(((NamedEntity)current).getEntity());
						//spurious.addCategory(_tagBase.getSpuriousTag());
						spurious.setSpurious();

						if (_useTags)
						{
							System.out.println(spurious + ALIGNMENT_CONNECTOR
									+ getComparableAlignments((NamedEntity) current, value));
						}
						else
						{
							clean = (NamedEntity) ((NamedEntity) current).unmarkTokens();
							System.out.println(spurious.unmarkTokens() + ALIGNMENT_CONNECTOR
									+ clean(getComparableAlignments((NamedEntity) current, value)));
						}
					}
				}
			}

			System.out.println(_tagBase.closeTag(_tagBase.getAltTag() + counter));
		}

		System.out.print(key.getClosingTag());
	}

	private String getAltItemString(LinkedList item)
	{
		String string = "";

		for (Iterator i = item.iterator(); i.hasNext();)
		{
			string += i.next().toString();
		}

		return string;
	}

	private boolean hasNamedEntity(LinkedList alternative)
	{
		for (Iterator i = alternative.iterator(); i.hasNext();)
		{
			if (i.next() instanceof NamedEntity)
			{
				return true;
			}
		}

		return false;
	}

	private LinkedHashSet getComparableAlignments(NamedEntity entity, Set aligned)
	{
		LinkedHashSet alignmentsToReturn = new LinkedHashSet();
		CounterTagParser parser = new CounterTagParser();
		LinkedList tokens = entity.split(parser);
		NamedEntity current;
		LinkedList alignedTokens;

		for (Iterator i = aligned.iterator(); i.hasNext();)
		{
			current = (NamedEntity) i.next();
			alignedTokens = current.split(parser);
			for (Iterator j = alignedTokens.iterator(); j.hasNext();)
			{
				if (tokens.contains(j.next()))
				{
					alignmentsToReturn.add(current);
					break;
				}
			}
		}

		if (alignmentsToReturn.isEmpty())
			alignmentsToReturn.add(null);

		return alignmentsToReturn;
	}

	/**
	 * Ponto de entrada no programa. Para o alinhamento ser efectuado é necessário que o ficheiro de submissão e
	 * o da Colecção Dourada sejam fornecidos como argumentos de entrada. Um terceiro argumento pode ser
	 * fornecido indicando se o output deve conter as etiquetas númericas utilizadas durante o alinhamento.
	 * 
	 * @param args
	 *          Array contendo os argumentos de entrada
	 */
	public static void main(String[] args)
	{
		String submission = null;
		String goldenSet = null;
		LinkedList ignorable;
		StopWordList stopWords;
		boolean viewTags = false;

		try
		{
			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equals("-submissao"))
				{
					i++;
					submission = args[i];
					continue;
				}

				if (args[i].equals("-cd"))
				{
					i++;
					goldenSet = args[i];
					continue;
				}

				if (args[i].equals("-etiquetas"))
				{
					i++;
					viewTags = args[i].equalsIgnoreCase("sim");
				}

				if (args[i].equals("-ignorar"))
				{
					i++;
					ignorable = getIgnorableWords(args[i]);
					stopWords = StopWordList.getInstance();
					for (Iterator k = ignorable.iterator(); k.hasNext();)
					{
						stopWords.addStopWord((String) k.next());
					}
				}
			}

			if (submission == null || goldenSet == null)
			{
				printSynopsis();
				return;
			}
		}
		catch (Exception ex)
		{
			printSynopsis();
			return;
		}

		/*		try {
			System.setOut(new PrintStream("ultima_saida_Aligner.txt"));
			System.err.println("inicio:");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		new Aligner(submission, goldenSet, viewTags);
	}

	private static LinkedList getIgnorableWords(String file)
	{
		BufferedReader reader = null;
		LinkedList toIgnore = new LinkedList();
		String buffer;
		try
		{
			reader = new BufferedReader(new FileReader(file));

			while ((buffer = reader.readLine()) != null)
			{
				toIgnore.add(buffer);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		try
		{
			reader.close();
		}
		catch (IOException e1)
		{
			;
		}

		return toIgnore;
	}

	/**
	 * No caso de os argumentos de entrada não serem correctamente fornecidos. Este método imprime uma mensagem
	 * de ajuda para a consola.
	 * 
	 */
	private static void printSynopsis()
	{
		System.out.println("Utiliza\u00e7\u00e3o:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 cp .;lib/jdom.jar [-Xmx512M] pt.linguateca.harem.Aligner -submissao <ficheiro_participacao> -cd <ficheiro_coleccao_dourada> [-etiquetas <sim|nao>]");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 cp .;lib/jdom.jar pt.linguateca.harem.Aligner -submissao participacao.xml -cd CDSegundoHAREM.xml");
	}
}