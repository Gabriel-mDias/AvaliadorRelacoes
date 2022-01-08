/*
 * Created on Jul 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class ReportKeyExtractor implements Runnable
{
	private final static String FILTER_TOKEN = "<br>Ficheiros utilizados:";

	private final static String DATE_TOKEN = "<br>Página gerada automaticamente em:";

	private final static String SEPARATOR = "------------------------------------------------------------------------------------";

	private String _file;

	public ReportKeyExtractor(String file)
	{
		_file = file;

		new Thread(this).start();
	}

	public void run()
	{
		BufferedReader reader;
		String buffer;

		String keyMap = null;
		String filter = null;
		String date = null;

		try
		{
			reader = new BufferedReader(new FileReader(_file));

			while ((buffer = reader.readLine()) != null)
			{
				if (buffer.equals(""))
					continue;

				if (buffer.charAt(0) == '{')
				{
					keyMap = buffer;
					continue;
				}

				if (buffer.startsWith(FILTER_TOKEN))
				{
					filter = buffer;
					continue;
				}

				if (buffer.startsWith(DATE_TOKEN))
				{
					date = buffer;
					System.out.println(filter.substring(4));
					System.out.println(date.substring(4));
					System.out.println("");
					System.out.println(keyMap);
					System.out.println("");
					System.out.println(SEPARATOR);
					System.out.println("");
					keyMap = null;
					filter = null;
					date = null;
					continue;
				}
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		new ReportKeyExtractor(args[0]);
	}
}
