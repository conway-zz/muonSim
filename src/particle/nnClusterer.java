/**
 * Adapts code from Norman Graf's ClusterFinding class
 * Will compare NN vs fixed cone from norman's class
 * @author Alex Conway
 */

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;
import org.lcsim.event.EventHeader;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.recon.cluster.nn.*;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;

public class nnClusterer extends Driver {
    
    private NearestNeighborClusterer _clusterer;
    private String[] _collectionNames;
    private AIDA aida = AIDA.defaultInstance();
    
    
    public nnClusterer()
    {
        int minCells = 35;
        int dU = 3;
        int dV = 3;
        int dLayer = 3;
        double threshold = 0.0001;
        _clusterer = new NearestNeighborClusterer(dU, dV, dLayer, minCells, threshold);
    }
    
    @Override
    protected void process(EventHeader event){
        
        /*
         * Directly from Norman's code
         * Creates a mapping of cells from all collections listed
         * identifies them by long cellID, which clusterers can use. 
         * Easy to include timing/energy cuts on hits
         */
        // which calorimeter hits should we cluster?
        Map<Long, CalorimeterHit> hitmap = new HashMap<Long, CalorimeterHit>();
        // which collections should we process?
        if (_collectionNames != null)
        {
            // only process the collections we requested
            for (int i = 0; i < _collectionNames.length; ++i)
            {
                List<SimCalorimeterHit> hits = event.getSimCalorimeterHits(_collectionNames[i]);
                for (SimCalorimeterHit h : hits)
                {
                    // apply any time or energy cuts here...
                    hitmap.put(h.getCellID(), h);
                }
            }
        } else
        {
            // process all calorimetercollections...
            List<List<SimCalorimeterHit>> hitList = event.get(SimCalorimeterHit.class);
            for (List<SimCalorimeterHit> l : hitList)
            {
                for (SimCalorimeterHit h : l)
                {
                    // apply any time or energy cuts here...
                    hitmap.put(h.getCellID(), h);
                }
            }
        }
        
        List<Cluster> clusters = _clusterer.createClusters(hitmap);
        
        String name = "nearestNeighbor";
        aida.cloud1D(name + "clusters").fill(clusters.size());
        Collections.sort(clusters, new ClusterSortByEnergy());
            
        /*
         * ### conway modification, took out these two lines ###
         * if (clusters.size() > 0)
                event.put(name + "_clusters", clusters);
         * 
         * ^^^^What is this?  What does EventHeader.put() do?
         * Also, interface specifies at least two more arguments...
         */
        
        int count = 0;
            for (Cluster cluster : clusters)
            {
                count++;
                double[] pos1 =
                {
                    0.0, 0.0, 0.0
                };
                double[] pos2 =
                {
                    0.0, 0.0, 0.0
                };
                double[] pos3 =
                {
                    0.0, 0.0, 0.0
                };
                double e1 = 0.0;
                double e2 = 0.0;
//                System.out.println("Cluster size:  " + cluster.getSize() + "  Energy:  " + cluster.getEnergy());
                if (count == 1)
                {
                    e1 = cluster.getEnergy();
                    aida.cloud1D(name + "energy first cluster").fill(cluster.getEnergy());
                    aida.cloud1D(name + "size first cluster").fill(cluster.getSize());
                    aida.cloud2D(name + " first cluster sizevsenergy"
                            ).fill(cluster.getSize(), cluster.getEnergy());
                    pos1 = cluster.getPosition();
                }
                if (count == 2)
                {
                    e2 = cluster.getEnergy();
                    aida.cloud1D(name + "energy second cluster").fill(cluster.getEnergy());
                    aida.cloud1D(name + "size second cluster").fill(cluster.getSize());
                    pos2 = cluster.getPosition();
                    pos3[0] = pos2[0] - pos1[0];
                    pos3[1] = pos2[1] - pos1[1];
                    pos3[2] = pos2[2] - pos1[2];
                    double delta = Math.sqrt(pos3[0] * pos3[0] + pos3[1] * pos3[1] + pos3[2] * pos3[2]);
                    //distances between first and second clusters
                    aida.cloud1D(name + "distance").fill(delta);
                    //differences in energy of first and second cluster
                    aida.cloud1D(name + "deltaE").fill(e1 - e2);
                    aida.cloud2D(name + "disvsdeltaE").fill(delta, e1 - e2);
                }
                if (count == 3)
                {
                    aida.cloud1D(name + "energy third cluster").fill(cluster.getEnergy());
                }
                if (count > 3)
                {
                    aida.cloud1D(name + "energy fourth and more cluster").fill(cluster.getEnergy());
                }

//
// Histogram the "corrected" energy
//
                aida.cloud1D(name + "energy").fill(cluster.getEnergy());
                aida.cloud1D(name + "size").fill(cluster.getSize());
                aida.cloud2D(name + "sizevsenergy").fill(cluster.getSize(), cluster.getEnergy());
//
// Histogram the position as R vs Z
//
                double[] pos = cluster.getPosition();
                double R = Math.sqrt(pos[0] * pos[0] + pos[1] * pos[1]);
                aida.cloud2D(name + "Position:R vs Z").fill(pos[2], R);
//
// Histogram the computed direction
//
                aida.cloud1D(name + "Direction: theta").fill(cluster.getITheta());
                aida.cloud1D(name + "Direction: phi").fill(cluster.getIPhi());
//
// Histogram the difference in direction and position theta,phi 
//
                double posphi = Math.atan2(pos[1], pos[0]);
                aida.cloud1D(name + "delta phi").fill(posphi - cluster.getIPhi());
                double postheta = Math.PI / 2. - Math.atan2(pos[2], R);
                aida.cloud1D(name + "delta theta").fill(postheta - cluster.getITheta());
            }
        
        /*
         * END : Directly from Norman's code
         */
        
        
    }
    /*
     * Below are directly lifted from Norman's code
     */

    public void setCollectionNames(String[] collectionNames)
    {
        _collectionNames = new String[collectionNames.length];
        System.arraycopy(collectionNames, 0, _collectionNames, 0, collectionNames.length);
    }
    public class ClusterSortBySize implements Comparator<Cluster>
    {

        @Override
        public int compare(Cluster cl1, Cluster cl2)
        {
            int diff = cl2.getSize() - cl1.getSize();
            return diff;
        }
    }

    public class ClusterSortByEnergy implements Comparator<Cluster>
    {

        @Override
        public int compare(Cluster cl1, Cluster cl2)
        {
            double diff = cl2.getEnergy() - cl1.getEnergy();
            if (diff > 0.0)
            {
                return 1;
            } else if (diff < 0.0)
            {
                return -1;
            } else
            {
                return 0;
            }
        }
    }
    
    
    
}
