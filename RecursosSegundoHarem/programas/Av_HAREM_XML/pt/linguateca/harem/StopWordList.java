/*
 * Created on Mar 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.util.HashSet;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class StopWordList
{
	private static StopWordList _instance;

	private HashSet _set;

	/**
	 * 
	 */
	private StopWordList()
	{
		_set = new HashSet();
		_set.add("de");
		//_set.add("DE");
		_set.add("De");

		_set.add("a");
		_set.add("A");

		_set.add("e");
		//_set.add("E");

		_set.add("o");
		_set.add("O");

		_set.add("do");
		_set.add("DO");
		_set.add("Do");

		_set.add("da");
		_set.add("DA");
		_set.add("Da");

		_set.add("que");
		_set.add("QUE");
		_set.add("Que");

		_set.add("em");
		_set.add("EM");
		_set.add("Em");

		_set.add("para");
		_set.add("PARA");
		_set.add("Para");

		_set.add("com");
		_set.add("COM");
		_set.add("Com");

		_set.add("os");
		_set.add("OS");
		_set.add("Os");

		_set.add("se");
		_set.add("SE");
		_set.add("Se");

		_set.add("um");
		_set.add("UM");
		_set.add("Um");

		_set.add("no");
		_set.add("NO");
		_set.add("No");

		_set.add("as");
		_set.add("AS");
		_set.add("As");

		_set.add("dos");
		_set.add("DOS");
		_set.add("Dos");

		_set.add("uma");
		_set.add("UMA");
		_set.add("Uma");

		_set.add("por");
		_set.add("POR");
		_set.add("por");

		_set.add("na");
		_set.add("NA");
		_set.add("Na");

		_set.add("n\00e3o");
		_set.add("N\00e3O");
		_set.add("N\00c3o");

		_set.add("ou");
		_set.add("OU");
		_set.add("Ou");

		_set.add("\00c0"); //À
		_set.add("\00c0s"); //Às

		_set.add("\u00e0"); //à
		_set.add("\u00e0s"); //às
		
		_set.add("ao");
		_set.add("AO");
		_set.add("Ao");

		_set.add("das");
		_set.add("DAS");
		_set.add("Das");

		_set.add("mais");
		_set.add("MAIS");
		_set.add("Mais");

		_set.add("for");
		_set.add("FOR");
		_set.add("For");

		_set.add("como");
		_set.add("COMO");
		_set.add("Como");

		_set.add("nos");
		_set.add("NOS");
		_set.add("Nos");

		_set.add("pelo");
		_set.add("PELO");
		_set.add("Pelo");

		_set.add("pela");
		_set.add("PELA");
		_set.add("Pela");

	}

	public void addStopWord(String word)
	{
		_set.add(word);
	}

	public static StopWordList getInstance()
	{
		if (_instance == null)
			_instance = new StopWordList();

		return _instance;
	}

	public boolean contains(String token)
	{
		return _set.contains(token);
	}

}
