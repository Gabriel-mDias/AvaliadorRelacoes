package pt.linguateca.util;

import java.util.Collection;
import java.util.*;

public class BucketHashMap extends BucketMap
{

  public BucketHashMap(Collection bucket)
  {
    super(bucket);
    _map = new HashMap();
  }

  public BucketHashMap(int capacity, Collection bucket)
  {
    super(bucket);
    _map = new HashMap(capacity);
  }

}