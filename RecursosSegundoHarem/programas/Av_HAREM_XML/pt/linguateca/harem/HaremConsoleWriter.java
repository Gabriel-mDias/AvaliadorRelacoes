/*
 * Created on Apr 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class HaremConsoleWriter
{
	protected boolean _useTags;

	protected static TagBase _tagBase;

	public HaremConsoleWriter(boolean useTags)
	{
		_useTags = useTags;
		_tagBase = TagBase.getInstance();
	}

	/**
	 * Alugumas sequências de caracteres são envolvidas em etiquetas durante o alinhamento. Este método remove
	 * essas etiquetas de forma a colocar o texto no seu formato original; tal como se encontrava no documento
	 * inicial.
	 * 
	 * @param entities
	 *          Lista de entidades a serem limpas
	 * @return Lista de entidades sem etiquetas númericas
	 */
	protected ArrayList clean(HashSet entities)
	{
		ArrayList clean = new ArrayList(entities.size());
		HaremEntity current;

		for (Iterator i = entities.iterator(); i.hasNext();)
		{
			current = (HaremEntity) i.next();
			clean.add(current == null ? null : current.unmarkTokens());
		}

		return clean;
	}
}
