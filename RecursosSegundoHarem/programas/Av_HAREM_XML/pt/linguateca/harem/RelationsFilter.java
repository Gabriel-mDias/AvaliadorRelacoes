package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import pt.linguateca.relations.Relation;
import pt.linguateca.relations.RelationProcessor;

/** avaliacao do ReRelEM - passo 2b - veus **/
public class RelationsFilter extends HaremEvaluator implements Runnable{

	private final String TYPE_SEP = ";";
	private final String ALL_TYPES = "Todas";

	private List<String> _validTypes;

	/** aceitar relacoes com argumentos espurios **/
	private boolean _acceptRelationsWithSpuriousArguments;

	public RelationsFilter(String alignmentFile, boolean useTags, String filter, boolean spurious)
	{
		super(alignmentFile, useTags);

		if(filter != null && !filter.equals(""))
			_validTypes = Arrays.asList(filter.split(TYPE_SEP));
		else
			_validTypes = null;

		_acceptRelationsWithSpuriousArguments = spurious;

		new Thread(this).start();
	}

	@Override
	public void run()
	{
		BufferedReader reader = null;
		String buffer;
		int state = -1;
		
		//HashMap<String, Set<String>> _categoriesInGC = new HashMap<String, Set<String>>();
		
		RelationProcessor processor = new RelationProcessor();
		Relation current;

		try
		{
			reader = new BufferedReader(new FileReader(_alignmentsFile));
			buffer = reader.readLine();

			//filtro classico
			if(buffer.startsWith("#")){
				System.out.println(buffer);
			}

			//imprimir o filtro ReRelEM
			if(_validTypes != null)
				System.out.println("#"+_validTypes.toString().replace("[", "").replace("]", ""));
			else
				System.out.println("#"+ALL_TYPES);

			while ((buffer = reader.readLine()) != null)
			{
				if(buffer.startsWith(_tagBase.getDocTag())){
					System.out.println("\n"+buffer);
					state = 0;

					continue;
				}

				else if(buffer.startsWith(_tagBase.getEndOfDocTag()))
				{
					System.out.println(buffer);
					state = -1;

					continue;
				}

				else if ((state == 0 || state == 1) && buffer.startsWith(AlignmentsToTriples.GC))
				{
					System.out.println(buffer);
					state = 1;
					continue;
				}

				else if ((state == 0 || state == 1) && buffer.startsWith(AlignmentsToTriples.PARTICIPATION))
				{
					System.out.println(buffer);
					state = 2;
					continue;
				}

				else if (buffer.trim().equals(""))
				{
					continue;
				}

				if(state == 1 || state == 2)
				{
					current = processor.getRelationM2(buffer);

					if(current != null)
					{
						//ignrar relacoes com argumentos espurios
						if(!_acceptRelationsWithSpuriousArguments && 
								(AlignmentSelector.isSpuriousId(current.getA()) ||
										AlignmentSelector.isSpuriousId(current.getB())))
						{
							continue;
						}

						else if(_validTypes == null)
							System.out.println(buffer);

						else
						{
							if(_validTypes.contains(current.getType()))
								System.out.println(current);
							
							else if(_validTypes.contains(Relation.OUTRA) || !current.isBasicType())
								System.out.println(current);
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

	public static void main(String args[])
	{
		String alignments = null;
		boolean useTags = false;
		boolean spurious = false;
		String filter = "";

		try{
			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equals("-alinhamento"))
				{
					i++;
					alignments = args[i];
					continue;
				}

				if (args[i].equals("-filtro"))
				{
					i++;
					filter = args[i];
					continue;
				}

				if (args[i].equals("-espurios"))
				{
					i++;
					spurious = args[i].equalsIgnoreCase("sim");
					continue;
				}

				if (args[i].equals("-etiquetas"))
				{
					i++;
					useTags = args[i].equalsIgnoreCase("sim");
					continue;
				}
			}

		} catch (Exception e)
		{
			printSynopsis();
			return;
		}

		new RelationsFilter(alignments, useTags, filter, spurious);
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
		.println("java -Dfile.encoding=ISO-8859-1 cp .;lib/jdom.jar pt.linguateca.harem.RelationsFilter -alinhamento <ficheiro_relacoes> [-filtro <filtro>] [-espurios <sim|nao>]");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 cp .;lib/jdom.jar pt.linguateca.harem.RelationsFilter -alinhamento ficheiro.alinhado.alts.expandido -filtro inclui;incluido");
	}
}
