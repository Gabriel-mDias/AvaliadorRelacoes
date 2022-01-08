/*
 * Created on Mar 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public abstract class Parser
{
	protected String _text;

	public int recognize(int start)
	{
		initialize();
		int index = doRecognition(start);
		if (index != -1)
		{
			createEntity();
		}
		return index;
	}

	public void setText(String text)
	{
		_text = text;
		initialize();
	}

	protected abstract int doRecognition(int start);

	protected abstract void initialize();

	protected abstract Object getEntity();

	protected abstract void createEntity();
}
