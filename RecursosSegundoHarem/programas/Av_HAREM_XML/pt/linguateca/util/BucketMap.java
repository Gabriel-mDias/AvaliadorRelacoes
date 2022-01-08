package pt.linguateca.util;

import java.util.*;

/**
 * <p>
 * Title: WORDNET 2.0 Model
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Nuno Seco
 * @version 1.0
 */

public abstract class BucketMap
{
  protected Map _map;

  private Class _bucketType;

  public BucketMap(Collection bucket)
  {
    _bucketType = bucket.getClass();
  }

  public void put(Object key)
  {
    try
    {
      Collection bucket = (Collection) _map.get(key);

      if (bucket == null)
      {
        bucket = (Collection) _bucketType.newInstance();
        _map.put(key, bucket);
      }
    }
    catch (IllegalAccessException ex)
    {
      ex.printStackTrace();
    }
    catch (InstantiationException ex)
    {
      ex.printStackTrace();
    }

  }

  public void put(Object key, Object value)
  {
    try
    {
      Collection bucket = (Collection) _map.get(key);

      if (bucket == null)
      {
        bucket = (Collection) _bucketType.newInstance();
        _map.put(key, bucket);
      }

      bucket.add(value);
    }
    catch (IllegalAccessException ex)
    {
      ex.printStackTrace();
    }
    catch (InstantiationException ex)
    {
      ex.printStackTrace();
    }
  }

  public Collection get(Object key)
  {
    return (Collection) _map.get(key);
  }

  public boolean containsKey(Object key)
  {
    return _map.containsKey(key);
  }

  public boolean containsValue(Object value)
  {
    Collection bucket;
    for (Iterator i = _map.values().iterator(); i.hasNext();)
    {
      bucket = (Collection) i.next();
      if (bucket.contains(value))
        return true;
    }

    return false;
  }

  public Set keySet()
  {
    return _map.keySet();
  }

  public Collection values()
  {
    return _map.values();
  }

  public String toString()
  {
    return _map.toString();
  }
}