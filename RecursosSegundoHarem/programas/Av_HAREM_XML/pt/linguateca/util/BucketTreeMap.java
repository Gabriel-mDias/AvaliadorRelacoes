package pt.linguateca.util;

import java.util.*;


public class BucketTreeMap extends BucketMap
{
  public BucketTreeMap(Collection bucket)
  {
    super(bucket);
    _map = new TreeMap();
  }

  public BucketTreeMap(Collection bucket, Comparator comparator)
  {
    super(bucket);
    _map = new TreeMap(comparator);
  }

}