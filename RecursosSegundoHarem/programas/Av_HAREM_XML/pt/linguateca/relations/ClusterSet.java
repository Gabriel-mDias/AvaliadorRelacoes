package pt.linguateca.relations;

import java.io.PrintStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

//import pt.linguateca.relations.Cluster;
import pt.linguateca.relations.Relation;

public class ClusterSet
{
    public static final String CLUSTER_NA = "NA";
    public static final String EVAL_IDENT_CLUSTER_HEADER = "Avalia\u00e7\u00e3o Identidade";

    private HashSet<String> _ids = new HashSet<String>();
    private HashMap<String, HashSet> _clusters = new HashMap<String, HashSet>();

    public ClusterSet(){
	_ids      = new HashSet<String>();
	_clusters = new HashMap<String, HashSet>();
    }
	
    public HashSet<String> getIDs() {
	return _ids;
    }
    
    public HashMap<String, HashSet> getClusters() {
	return _clusters;
    }

    public void clear() {
	_ids.clear();
	_clusters.clear();
    }

    public void update(Relation relation) {
	String a = relation.getA();
	String b = relation.getB();
	
	String clusterIDofA = getClusterID(a); 
	String clusterIDofB = getClusterID(b); 

	HashSet<String> temp = new HashSet<String>();
	
	
	if( CLUSTER_NA.equals(clusterIDofA) && CLUSTER_NA.equals(clusterIDofB)){
	    temp.add(a);
	    _clusters.put(a,temp);
	    _clusters.get(a).add(b);
	    _ids.add(a);
	    _ids.add(b);
	    return;
	}

	if( !CLUSTER_NA.equals(clusterIDofA) && !CLUSTER_NA.equals(clusterIDofB)){
	    if( !clusterIDofA.equals(clusterIDofB) ){
		_clusters.get(clusterIDofA).addAll(_clusters.get(clusterIDofB));
		_clusters.remove(clusterIDofB);
		_ids.add(clusterIDofB);
		return;
	    }
	}
	if( !CLUSTER_NA.equals(clusterIDofA)){
	    _clusters.get(clusterIDofA).add(b);
	    _ids.add(b);
	    return;
	}
	if( !CLUSTER_NA.equals(clusterIDofB)){
	    _clusters.get(clusterIDofB).add(a);
	    _ids.add(a);
	    return;
	}
	System.out.println("Falhou qq coisa!!!");
    }


    public void update(String entity) {
	
	HashSet<String> temp = new HashSet<String>();
	
	String clusterID = getClusterID(entity);
	
	if( CLUSTER_NA.equals( clusterID ) ){
	    temp.add(entity);
	    _clusters.put(entity,temp);
	    _ids.add(entity);
	}
    }

    public void output(PrintStream outputStream){
	Iterator<String> i;
	Iterator<String> j;

	outputStream.println(_clusters.size());
	for (i = _clusters.keySet().iterator(); i.hasNext();){
	    String clusterID = i.next();
	    outputStream.print(clusterID + ": ");
	    for (j = _clusters.get(clusterID).iterator(); j.hasNext();){
		String entity = j.next();
		outputStream.print(entity + " ");
	    }
	    outputStream.println("");
	}
    }
    
    public String getClusterID( String entity ){
	Iterator<String> i;
	
	if( _clusters.containsKey(entity) ){
	    return entity;
	}
	else{
	    for (i = _clusters.keySet().iterator(); i.hasNext();){
		String clusterID = i.next();
		if( _clusters.get(clusterID).contains(entity)){
		    return clusterID;
		}
	    }
	}
	return CLUSTER_NA;
    }

    // clusterSet2 is the reference cluster set
    public void compare( ClusterSet clusterSet2 ){

	Iterator<String> i1;
	Iterator<String> i2;

	HashSet<String> cluster1;
	HashSet<String> cluster2;
	HashSet<String> intersection;

	HashMap<String, HashSet> clusters2 = clusterSet2.getClusters();
	String clusterID1;
	String clusterID2;

	double precision = 0;
	double recall = 0;
	double fMeasure = 0;
	double spurious = 0;
	double missing = 0;

	double clusterPrecision = 0;
	double clusterRecall = 0;
	double clusterFMeasure = 0;
	double clusterSpurious = 0;
	double clusterMissing = 0;

	double totalPrecision = 0;
	double totalRecall = 0;
	double totalFMeasure = 0;
	double totalSpurious = 0;
	double totalMissing = 0;

	int nClustersOverlapped = 0;

	for (i1 = _clusters.keySet().iterator(); i1.hasNext();){
	    clusterID1 = i1.next();
	    cluster1 = _clusters.get(clusterID1);
	    
	    System.out.print(clusterID1 + ": ");
	    
	    clusterPrecision = 0;
	    clusterRecall = 0;
	    clusterFMeasure = 0;
	    clusterSpurious = 0;
	    clusterMissing = 0;
	    nClustersOverlapped = 0;
	    for (i2 = clusters2.keySet().iterator(); i2.hasNext();){
		clusterID2 = i2.next();
		cluster2 = clusters2.get(clusterID2);
		intersection = new HashSet<String>(cluster2);
		intersection.retainAll( cluster1 );
		if( intersection.size() != 0 ){
		    precision = ((double)intersection.size()) / cluster1.size();
		    recall = ((double)intersection.size()) / cluster2.size();
		    clusterPrecision += precision;
		    clusterRecall += recall;
		    clusterFMeasure += (2 * precision * recall / (precision + recall));
		    clusterSpurious += (((double)cluster1.size() - intersection.size()) / cluster1.size());
		    clusterMissing += (((double)cluster2.size() - intersection.size()) / cluster2.size());
		    System.out.print(clusterID2 + " [P: " + intersection.size()+"/"+cluster1.size() 
				                + " A: " + intersection.size()+"/"+cluster2.size()
				                + " S: " + (cluster1.size()-intersection.size()) +"/"+cluster1.size() 
		                                + " M: " + (cluster2.size()-intersection.size()) +"/"+cluster2.size() + "]; ");
		    nClustersOverlapped++;
		}
	    }
	    if(nClustersOverlapped != 0){
		totalPrecision += (clusterPrecision/nClustersOverlapped);
		totalRecall += (clusterRecall/nClustersOverlapped);
		totalFMeasure += (clusterFMeasure/nClustersOverlapped);
		totalSpurious += clusterSpurious/nClustersOverlapped;
		totalMissing += clusterMissing/nClustersOverlapped;
		System.out.println(">>>> CLUSTER part: " + clusterID1 + " [P: " + (clusterPrecision/nClustersOverlapped) 
				                                      + " A: " + (clusterRecall/nClustersOverlapped) 
				                                      + " S: " + (clusterSpurious/nClustersOverlapped) 
				                                      + " M: " + (clusterMissing/nClustersOverlapped) + "]; ");
	    }
	    else{
		totalPrecision += clusterPrecision;
		totalRecall += clusterRecall;
		totalFMeasure += clusterFMeasure;
		totalSpurious += clusterSpurious;
		totalMissing += clusterMissing;
		System.out.println(" >>>> CLUSTER part: " + clusterID1 + " [P: " + clusterPrecision 
				                                       + " A: " + clusterRecall 
		                                                       + " S: " + clusterSpurious 
	                                                               + " M: " + clusterMissing + "]; ");
	    }
	}
	System.out.println("[" + EVAL_IDENT_CLUSTER_HEADER + ": P(" + totalPrecision/_clusters.keySet().size() 
			                                   + ") A(" + totalRecall/_clusters.keySet().size() 
			                                   + ") S(" + totalSpurious/_clusters.keySet().size() 
			                                   + ") M(" + totalMissing/_clusters.keySet().size() + ")]"); 
    }
}
