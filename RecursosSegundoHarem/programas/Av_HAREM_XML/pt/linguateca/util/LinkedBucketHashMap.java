/*
 * Created on Mar 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * @author nseco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LinkedBucketHashMap extends BucketMap
{

	public LinkedBucketHashMap(Collection bucket)
	{
		super(bucket);
		_map = new LinkedHashMap();
	}

	public LinkedBucketHashMap(int capacity, Collection bucket)
	{
		super(bucket);
		_map = new LinkedHashMap(capacity);
	}
}
