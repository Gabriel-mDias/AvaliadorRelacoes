package pt.linguateca.harem;

import org.jdom.Text;
import org.jdom.filter.Filter;

public class TextFilter implements Filter{

	public boolean matches(Object arg0) {
		
		return (arg0 instanceof Text);
	}

}
