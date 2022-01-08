package pt.linguateca.harem.reports;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import pt.linguateca.harem.AlignmentsToTriples;
import pt.linguateca.harem.TagBase;
import pt.linguateca.relations.RelationM2;
import pt.linguateca.relations.RelationProcessor;

/**
 * Todas as relações num ficheiro de triplas, apos a etiqueta [CD]
 * Entrada: ficheiro de triplas
 * Para verificar estes dados para um ficheiro de alinhamentos, utilizar primeiro AlignmentsToTriples
 * @author Besugo
 *
 */
public class PrintRelationsM2 implements Runnable{

	private String _file;
	private boolean _onlyOneWayRelations;

	public PrintRelationsM2(String file, boolean oneWay){

		this._file = file;
		this._onlyOneWayRelations = oneWay;
		new Thread(this).start();
	}

	public void run()
	{	
		TagBase tagBase = TagBase.getInstance();

		BufferedReader reader = null;
		String buffer;
		int state = -1;

		RelationProcessor processor = new RelationProcessor();
		Set<RelationM2> relations = null;
		RelationM2 current;
		RelationM2 inverse;

		try
		{
			reader = new BufferedReader(new FileReader(_file));

			while ((buffer = reader.readLine()) != null)
			{
				//filtros
				if(buffer.startsWith("#")){
					continue;
				}		

				if(buffer.startsWith(tagBase.getDocTag())){
					System.out.println("\n"+buffer);
					state = 0;

					relations = new HashSet<RelationM2>();
					continue;
				}

				else if(buffer.startsWith(tagBase.getEndOfDocTag()))
				{
					printRelations(relations);
					System.out.println(buffer);
					state = -1;

					continue;
				}

				else if ((state == 0 || state == 1) && buffer.startsWith(AlignmentsToTriples.GC))
				{
					state = 1;
					continue;
				}

				else if ((state == 0 || state == 1) && buffer.startsWith(AlignmentsToTriples.PARTICIPATION))
				{
					state = 2;
					continue;
				}

				else if (buffer.trim().equals(""))
				{
					continue;
				}

				current = processor.getRelationM2(buffer);

				if(state == 1)
				{
					if(!_onlyOneWayRelations)
					{
						System.out.println(current);
					}
					else
					{

						inverse = (RelationM2)tagBase.getInverse(current);

						if(inverse != null && !tagBase.isDirectRelationType(current.getType()))
						{
							RelationM2 tmp = inverse;
							inverse = current;
							current = tmp;
						}

						if(!relations.contains(current) && !relations.contains(inverse))
							relations.add(current);
					}
				}

				if(state == 2)
				{
					continue;
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

	private void printRelations(Set<RelationM2> relations)
	{
		for(RelationM2 relation : relations)
			System.out.println(relation);
	}

	public static void main(String args[]){

		String file = null;
		boolean onlyOneWayRelations = false;

		try{
			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equals("-entrada"))
				{
					i++;
					file = args[i];
					continue;
				}

				if (args[i].equals("-apenas_directas"))
				{
					onlyOneWayRelations = true;
					continue;
				}
			}

		} catch (Exception e)
		{
			printSynopsis();
			return;
		}

/*		try {
			System.setOut(new PrintStream("relacoes.csv"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		new PrintRelationsM2(file, onlyOneWayRelations);
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
		.println("java -Dfile.encoding=ISO-8859-1 cp .;lib/jdom.jar pt.linguateca.harem.RelationsEvaluatorM2 -alinhamento <ficheiro_relacoes>");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 cp .;lib/jdom.jar pt.linguateca.harem.RelationsEvaluatorM2 -alinhamento ficheiro.alinhado.alts.expandido");
	}
}
