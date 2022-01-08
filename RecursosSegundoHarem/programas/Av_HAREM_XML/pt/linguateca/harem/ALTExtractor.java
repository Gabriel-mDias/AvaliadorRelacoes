/*
 * Created on Sep 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class ALTExtractor implements Runnable
{

	private String _file;

	/**
	 * 
	 */
	public ALTExtractor(String file)
	{
		_file = file;
		new Thread(this).start();
	}

	public void run()
	{
		BufferedReader reader = null;
		boolean out = false;
		String buffer;

		try
		{
			reader = new BufferedReader(new FileReader(_file));

			while ((buffer = reader.readLine()) != null)
			{
				if (buffer.equals("<ALT>"))
				{
					out = true;
					System.out.println("");
				}

				if (out)
				{
					System.out.println(buffer);
				}

				if (buffer.equals("</ALT>"))
				{
					out = false;
					System.out.println("");
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
		new ALTExtractor(args[0]);
	}
}
