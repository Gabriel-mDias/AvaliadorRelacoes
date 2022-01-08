/*
 * Created on Apr 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import pt.linguateca.util.LinkedBucketHashMap;

/**
 * @author nseco
 * 
 * Esta classe define a estrutura utilizada para guardar os alinhamentos e os possiveis erros ocorridos.
 */
public class AlignmentMap
{
	private String _docID;

	private String _origin;

	private String _genre;

	private LinkedBucketHashMap _alignments;

	private LinkedBucketHashMap _faults;

	private HashMap _scores;

	private TagBase _tagBase;

	public AlignmentMap()
	{
		_alignments = new LinkedBucketHashMap(new LinkedHashSet());
		_faults = new LinkedBucketHashMap(new LinkedHashSet());
		_scores = new HashMap();
		_tagBase = TagBase.getInstance();
	}

	public void putAlignment(HaremEntity entityToMap, HaremEntity mappedEntity)
	{
		//System.out.println(entityToMap+" --- "+mappedEntity);
		_alignments.put(entityToMap, mappedEntity);
	}

	public void putAllAlignments(HaremEntity entity, Collection mappings)
	{
		Iterator i = mappings.iterator();
		_alignments.put(entity);
		while (i.hasNext())
		{
			_alignments.put(entity, (HaremEntity) i.next());
		}
	}

	public void putFaults(HaremEntity entityToMap, String faultyToken)
	{
		_faults.put(entityToMap, faultyToken);
	}

	public boolean hasAlignment(HaremEntity mappedEntity)
	{
		Set<HaremEntity> set;
		for (Iterator<Set<HaremEntity>> i = _alignments.values().iterator(); i.hasNext();)
		{
			set = i.next();
			if (set != null && set.contains(mappedEntity))
				return true;
		}
		return false;
	}

	public int getAlignementsSize(){
		return _alignments.values().size();
	}
	
	public Iterator getAlignmentsIterator()
	{
		return _alignments.keySet().iterator();
	}

	public LinkedHashSet getAlignment(HaremEntity key)
	{
		return (LinkedHashSet) _alignments.get(key);
	}

	public String getScore(HaremEntity key)
	{
		return _scores.get(key).toString();
	}

	public Iterator getFaultyAlignmentsIterator()
	{
		return _faults.keySet().iterator();
	}

	public LinkedHashSet getFaultyTokens(HaremEntity key)
	{
		return (LinkedHashSet) _faults.get(key);
	}

	public boolean hasFaults()
	{
		return !_faults.keySet().isEmpty();
	}

	public void setDocID(String id)
	{
		_docID = id;
	}

	public String getDocID()
	{
		return _docID;
	}

	public void setOrigin(String origin)
	{
		_origin = origin;
	}

	public String getOrigin()
	{
		return _origin;
	}

	public void setGenre(String genre)
	{
		_genre = genre;
	}

	public String getGenre()
	{
		return _genre;
	}

}
