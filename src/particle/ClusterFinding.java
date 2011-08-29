
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.lcsim.event.Cluster;
import org.lcsim.event.EventHeader;
import org.lcsim.recon.cluster.nn.NearestNeighborClusterDriver;
import org.lcsim.recon.cluster.fixedcone.FixedConeClusterDriver;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;

/**
 * An example that shows how to find clusters and make some simple plots
 * of the results.
 * 
 * @author Norman Graf
 * @version $Id: ClusterFinding.java,v 1.1 2008/10/30 23:38:19 jeremy Exp $
 * 
 */
public class ClusterFinding extends Driver {

    public class ClusterSortBySize implements Comparator<Cluster> {

        @Override
        public int compare(Cluster cl1, Cluster cl2) {
            int diff = cl2.getSize() - cl1.getSize();
            return diff;
        }
    }

    public class ClusterSortByEnergy implements Comparator<Cluster> {

        @Override
        public int compare(Cluster cl1, Cluster cl2) {
            double diff = cl2.getEnergy() - cl1.getEnergy();
            if (diff > 0.0) {
                return 1;
            } else if (diff < 0.0) {
                return -1;
            } else {
                return 0;
            }
        }
    }
//
// Use the "convenient" method of generating AIDA plots 
//
    private AIDA aida = AIDA.defaultInstance();

    public ClusterFinding() {
//
//    Add a cluster Driver with required parameters
//
        int minCells = 35;
        int dU = 3;
        int dV = 3;
        int dLayer = 3;
        String[] hitcollnames = {
            "EcalBarrelHits",
            "EcalEndcapHits",
            "HcalBarrelHits",
            "HcalEndcapHits",
            "MuonBarrelHits",
            "MuonEndcapHits"};
        /*
        NearestNeighborClusterDriver nncldr = new NearestNeighborClusterDriver(dU, dV, dLayer, minCells);
        nncldr.setCollectionNames(hitcollnames);
        add(nncldr);
//        add(new NearestNeighborClusterDriver(minCells));

        double radius = .4;
        double seed = 0.;
        double minE = 0.;
//        String[] hitcollnames = {"EcalBarrelHits", "EcalEndcapHits"};
        FixedConeClusterDriver fcd = new FixedConeClusterDriver(radius, seed, minE);
        fcd.setCollectionNames(hitcollnames);
        add(fcd);

         * 
         */
        //        add(new FixedConeClusterDriver(radius, seed, minE));

    }
//
// Process an event
//

    @Override
    protected void process(EventHeader event) {
        
//
// Make clusters
//
        super.process(event);
        //       System.out.println("Event:  " + event.getEventNumber());
        List<Cluster> EcalClusters = (List<Cluster>) event.get("EcalBarrelHitsFixedConeClusters");
        List<Cluster> HcalClusters = (List<Cluster>) event.get("HcalBarrelHitsFixedConeClusters");
        for (Cluster ecalcluster : EcalClusters) {
            double EcalIPhi=ecalcluster.getIPhi();
            double EcalITheta=ecalcluster.getITheta();
            
        }
        //System.out.println("Ecal number of clusters:  " + EcalClusters.size());

//      
// Find all the cluster Lists
//
        List<List<Cluster>> clusterSets = event.get(Cluster.class);

        aida.cloud1D("clusterSets").fill(clusterSets.size());
//       List<Cluster> clusters=clusterSets.get(1);
//
// Loop over all the cluster Lists
//     
        for (List<Cluster> clusters : clusterSets) {
//
// Get the ClusterList name
//
            String name = event.getMetaData(clusters).getName() + "/";
//
// Histogram the number of clusters in the List
//

            aida.cloud1D(name + "clusters").fill(clusters.size());
            Collections.sort(clusters, new ClusterSortByEnergy());

            // Loop over all the clusters in a List
            //
            int count = 0;
            for (Cluster cluster : clusters) {
                count++;
                double[] pos1 = {0.0, 0.0, 0.0};
                double[] pos2 = {0.0, 0.0, 0.0};
                double[] pos3 = {0.0, 0.0, 0.0};
                double e1 = 0.0;
                double e2 = 0.0;
//                System.out.println("Cluster size:  " + cluster.getSize() + "  Energy:  " + cluster.getEnergy());
                if (count == 1) {
                    e1 = cluster.getEnergy();
                    aida.cloud1D(name + "energy first cluster").fill(cluster.getEnergy());
                    aida.cloud1D(name + "size first cluster").fill(cluster.getSize());
                    pos1 = cluster.getPosition();
                }
                if (count == 2) {
                    e2 = cluster.getEnergy();
                    aida.cloud1D(name + "energy second cluster").fill(cluster.getEnergy());
                    aida.cloud1D(name + "size second cluster").fill(cluster.getSize());
                    pos2 = cluster.getPosition();
                    pos3[0] = pos2[0] - pos1[0];
                    pos3[1] = pos2[1] - pos1[1];
                    pos3[2] = pos2[2] - pos1[2];
                    double delta = Math.sqrt(pos3[0] * pos3[0] + pos3[1] * pos3[1] + pos3[2] * pos3[2]);
                    aida.cloud1D(name + "distance").fill(delta);
                    aida.cloud1D(name + "deltaE").fill(e1 - e2);
                    aida.cloud2D(name + "disvsdeltaE").fill(delta, e1 - e2);
                }
                if (count == 3) {
                    aida.cloud1D(name + "energy third cluster").fill(cluster.getEnergy());
                }
                if (count > 3) {
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
        }
    }
}
