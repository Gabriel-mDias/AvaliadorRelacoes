/*
 * Created on Aug 23, 2005
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
public class HAREM2HTML implements Runnable
{
	private String _file;

	private boolean _numbering;

	private boolean _comment;

	public HAREM2HTML(String file, boolean numbering, boolean comment)
	{
		_file = file;
		_numbering = numbering;
		_comment = comment;
		new Thread(this).start();
	}

	public void run()
	{
		BufferedReader reader = null;
		String buffer;
		int count = 0;
		try
		{
			reader = new BufferedReader(new FileReader(_file));

			while ((buffer = reader.readLine()) != null)
			{
				if (buffer.equals(""))
					continue;

				buffer = buffer.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

				if (_numbering && !buffer.startsWith("#") && !buffer.startsWith("HAREM"))
				{
					count++;
					System.out.println("<font color=\"blue\">");
					System.out.println(count + ".");
					System.out.println("</font>");
				}

				System.out.println(buffer);

				if (_comment && !buffer.startsWith("#") && !buffer.startsWith("HAREM"))
				{
					System.out.println("<br>");
					System.out.println("<font color=\"blue\">");
					System.out.println("Comentário:");
					System.out.println("</font>");
				}

				System.out.println("<br>");
				System.out.println("<br>");
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public static void main(String[] args)
	{
		String file = null;
		boolean numbering = false;
		boolean comment = false;

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-ficheiro"))
			{
				file = args[++i];
				continue;
			}

			if (args[i].equals("-numeracao"))
			{
				numbering = true;
				continue;
			}

			if (args[i].equals("-comentario"))
			{
				comment = true;
				continue;
			}
		}

		new HAREM2HTML(file, numbering, comment);
	}

}
