package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;

import pt.linguateca.relations.ClusterSet;
import pt.linguateca.relations.Relation;
import pt.linguateca.relations.RelationM2;
import pt.linguateca.relations.RelationProcessor;

/** avaliacao do ReRelEM - aglomerador **/
public class ClusterRelations extends HaremEvaluator implements Runnable{

	private PrintStream _outputStream = null;

	private HashSet<Relation> updateRelationsWithClusters(HashSet<Relation> relations, ClusterSet identClusters){
		HashSet<Relation> updatedRelations = new HashSet<Relation>();

		Iterator<Relation> i;
		Relation rel;
		String updatedA;
		String updatedB;

		for( i = relations.iterator(); i.hasNext();){
			rel = i.next();
			updatedA = identClusters.getClusterID(rel.getA());
			updatedB = identClusters.getClusterID(rel.getB());

			if( updatedA.equals(ClusterSet.CLUSTER_NA)){
				updatedA = updatedA + " " + rel.getA();
			}
			if( updatedB.equals(ClusterSet.CLUSTER_NA)){
				updatedB = updatedB + " " +rel.getB();
			}
			updatedRelations.add(new Relation(rel.getType(), updatedA, updatedB));
		}
		return updatedRelations;
	}

	public ClusterRelations(String alignmentFile, boolean useTags, String logFile){

		super(alignmentFile, useTags);

		if(logFile != null && !logFile.equals("")){
			try {
				_outputStream = new PrintStream(logFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		new Thread(this).start();
	}

	@Override
	public void run(){
		BufferedReader reader = null;
		String buffer;
		int state = -1;

		RelationProcessor processor = new RelationProcessor();
		RelationM2 current;
		ClusterSet clustersGC = new ClusterSet();
		ClusterSet clustersPart = new ClusterSet();

		HashSet<Relation> relationsGC = new HashSet<Relation>();
		HashSet<Relation> relationsPart = new HashSet<Relation>();

		HashSet<Relation> updatedRelationsGC; 
		HashSet<Relation> updatedRelationsPart; 

		String a, b;

		Relation rel;

		Iterator<Relation> i;    

		try {
			reader = new BufferedReader(new FileReader(_alignmentsFile));
			buffer = reader.readLine();

			//filtro classico
			if(buffer.startsWith("#")){
				System.out.println(buffer);
			}

			while ((buffer = reader.readLine()) != null){
				if(buffer.startsWith(_tagBase.getDocTag())){
					System.out.println("\n"+buffer);
					if( _outputStream != null ){
						_outputStream.println(buffer);
					}
					state = 0;

					continue;
				}

				else if(buffer.startsWith(_tagBase.getEndOfDocTag())){
					if( _outputStream != null ){
						_outputStream.println(AlignmentsToTriples.GC);
						clustersGC.output(_outputStream);
						_outputStream.println(AlignmentsToTriples.PARTICIPATION);
						clustersPart.output(_outputStream);
						_outputStream.println(buffer);
					}

					//clustersPart.compare(clustersGC);

					updatedRelationsGC = updateRelationsWithClusters(relationsGC,clustersGC);
					updatedRelationsPart = updateRelationsWithClusters(relationsPart,clustersPart);

					System.out.println(AlignmentsToTriples.GC);
					for( i = updatedRelationsGC.iterator(); i.hasNext();){
						rel = i.next();
						System.out.println(rel.toString());
					}
					System.out.println(AlignmentsToTriples.PARTICIPATION);
					for( i = updatedRelationsPart.iterator(); i.hasNext();){
						rel = i.next();
						System.out.println(rel.toString());
					}

					relationsGC.clear();
					relationsPart.clear();

					updatedRelationsGC.clear(); 
					updatedRelationsPart.clear(); 

					clustersGC.clear();
					clustersPart.clear();

					System.out.println(buffer);

					state = -1;

					continue;
				}

				else if ((state == 0 || state == 1) && buffer.startsWith(AlignmentsToTriples.GC)){
					//System.out.println(buffer);
					state = 1;
					continue;
				}

				else if ((state == 0 || state == 1) && buffer.startsWith(AlignmentsToTriples.PARTICIPATION)){
					//System.out.println(buffer);
					state = 2;
					continue;
				}

				else if (buffer.trim().equals("")){
					continue;
				}

				if(state == 1){
					current = processor.getRelationM2(buffer);

					if(current != null)
					{

						if( current.getType().equals(current.IDENTIDADE)){
							relationsGC.add(current);
							clustersGC.update(current);
						} else 
						{
							clustersGC.update(current.getA());
							clustersGC.update(current.getB());
							relationsGC.add(current);
						}
					}
				}

				if(state == 2){
					current = processor.getRelationM2(buffer);

					if(current != null)
					{

						if( current.getType().equals(current.IDENTIDADE)){
							relationsGC.add(current);
							clustersPart.update(current);
						} else 
						{
							clustersPart.update(current.getA());
							clustersPart.update(current.getB());
							relationsPart.add(current);
						}
					}
				}
			}
		}

		catch (IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			reader.close();
		}
		catch (IOException e){
			e.printStackTrace();
		}


	}

	public static void main(String args[]){
		String alignments = null;
		boolean useTags = false;
		String logFile = "";

		for (int i = 0; i < args.length; i++){
			if (args[i].equals("-alinhamento")){
				i++;
				alignments = args[i];
				continue;
			}

			if (args[i].equals("-debug")){
				i++;
				logFile = args[i];
				continue;
			}

			if (args[i].equals("-etiquetas")){
				i++;
				useTags = args[i].equalsIgnoreCase("sim");
				continue;
			}
		}
		new ClusterRelations(alignments, useTags, logFile);
	}

}
