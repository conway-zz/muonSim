/*
 * Want: Width and mean of energy distributions
 * given sliding scale of time cuts.
 */
import java.util.List;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.MCParticle;
import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;
import hep.aida.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.lcsim.event.Cluster;
import org.lcsim.recon.cluster.nn.NearestNeighborClusterDriver;
import org.lcsim.recon.cluster.fixedcone.FixedConeClusterDriver;
import org.lcsim.util.aida.AIDA;
/**
 *
 * @author agias
 */
public class energyResolution extends Driver {
    
    //From Norman Graf's ClusterFinding.java
    public class ClusterSortBySize implements Comparator<Cluster> {

        @Override
        public int compare(Cluster cl1, Cluster cl2) {
            int diff = cl2.getSize() - cl1.getSize();
            return diff;
        }
    }
    //From Norman Graf's ClusterFinding.java
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
    
    //Get the cluster closest to the input position from list of clusters
    //TODO: add distance restriction; return null if too far away.
    public Cluster getClosestCluster(List<Cluster> clusters, double[] position){
        double minRadTemp = 0;
        double minRadSet = 0;
        boolean firstRun = true;
        Cluster rv = null;
        //apply distance formula to get minRad from first cluster
        //If minRad changes, set active cluster to rv
        //This may not be best solution; must check how getPosition() works
        for(Cluster cluster : clusters){
            double[] newPos = cluster.getPosition();
            if(firstRun == true){
                minRadTemp = Math.sqrt(
                        Math.pow(newPos[0] - position[0], 2)+
                        Math.pow(newPos[1] - position[1], 2)+
                        Math.pow(newPos[2] - position[2], 2));
                firstRun = false;
                minRadSet = minRadTemp;
                rv = cluster;
            }
            else{
                minRadTemp = Math.min(minRadTemp,
                        Math.sqrt(
                        Math.pow(newPos[0] - position[0], 2)+
                        Math.pow(newPos[1] - position[1], 2)+
                        Math.pow(newPos[2] - position[2], 2)));
                if(minRadTemp != minRadSet){
                    rv = cluster;
                    minRadSet = minRadTemp;
                }
            }
            
        }
        return rv;
    }
    
    // Use the "convenient" method of generating AIDA plots 
    //
    private AIDA aida;
     IAnalysisFactory af;
     ITree TREE;
     IHistogramFactory hf;
     //IHistogram1D totalEnergies;
     IHistogram1D cutEnergies;
     IHistogram2D cutEnergies2D;
     IHistogram1D meanEnergies;
     IHistogram1D widthEnergies;
     IHistogram1D tempEnergies;
     IHistogram1D mipEM;
     IHistogram1D mipH;
     IHistogram1D mipMu;
     IHistogram1D mipCutEM;
     IHistogram1D mipCutH;
     IHistogram1D mipCutMu;
     IHistogram1D mipCutEMb;
     IHistogram1D mipCutHb;
     IHistogram1D mipCutMub;
     int bins;
     double min;
     double max;
     double EVENTS;
    
    protected void startOfData(){
        aida = AIDA.defaultInstance();
        af = IAnalysisFactory.create();
        TREE = af.createTreeFactory().create();
        hf = af.createHistogramFactory(TREE);
        bins = 200;
        min = 0;
        max = 1000;
        cutEnergies = hf.createHistogram1D(
                "cutEnergies",
                bins,min,max);
        cutEnergies2D = hf.createHistogram2D(
                "cutEnergies2D",
                bins,min,max,
                100,0,10);
mipEM = hf.createHistogram1D("mipEM",200,0,0.2);
mipH = hf.createHistogram1D("mipH",200,0,0.2);
mipMu = hf.createHistogram1D("mipMu",200,0,0.2);
mipCutEM = hf.createHistogram1D("mipCutsEM",200,0,10);
mipCutH = hf.createHistogram1D("mipCutsH",200,0,10);
mipCutMu = hf.createHistogram1D("mipCutsMu",200,0,10);
mipCutEMb = hf.createHistogram1D("mipCutsEMb",200,0,10);
mipCutHb = hf.createHistogram1D("mipCutsHb",200,0,10);
mipCutMub = hf.createHistogram1D("mipCutsMub",200,0,10);
    }
    //From Norman Graf's ClusterFinding.java, modified by Alex Conway
    //not using fixedConeClustering because it's much worse than nn
    public energyResolution() {
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
        NearestNeighborClusterDriver nncldr = new NearestNeighborClusterDriver(dU, dV, dLayer, minCells);
        nncldr.setCollectionNames(hitcollnames);
        add(nncldr);
/*
        double radius = .4;
        double seed = 0.;
        double minE = 0.;
//        String[] hitcollnames = {"EcalBarrelHits", "EcalEndcapHits"};
        FixedConeClusterDriver fcd = new FixedConeClusterDriver(radius, seed, minE);
        fcd.setCollectionNames(hitcollnames);
        add(fcd);
         * 
         */
    }
        
    @Override
    protected void process(EventHeader event){
        
        //process drivers added with add(<T>Driver) in constructor
        //super.process(event);
        //clusterProcess(event);
        mipProcess(event);
        
    }
    
    public void clusterProcess(EventHeader event){
        //get all clusters
        List<List<Cluster>> clusterSets = event.get(Cluster.class);
        
        //list of clusters, one per detector, 
        //that are hopefully spatially contiguous
        List<Cluster> linkedClusters = new ArrayList<Cluster>();
                
        //Just shows how many sets of clusters we're dealing with.
        //should be equal to length of hitcollnames times clustering algorithms
        aida.cloud1D("clusterSets").fill(clusterSets.size());
        
        boolean firstRun = true;
        double[] primePosition;
        //loop over sets of clusters
        //large amounts of this loop taken from Graf
        for(List<Cluster> clusters : clusterSets) {
            
            if(clusters.size()>0){
            //get name of each cluster set, append '/' so it makes its own 
            //AIDA folder later. Nifty
            String name = event.getMetaData(clusters).getName() + "/";
            
            //First histogram, simply records number of clusters in the event/detector
            aida.cloud1D(name + "clusters").fill(clusters.size());
            //sort by energy, first is 'prime' cluster
            Collections.sort(clusters, new ClusterSortByEnergy());
            
            primePosition = clusters.get(0).getPosition();
            //use highest energy cluster of first list as primary cluster, 
            //add clusters from other collections closest to primary
            //getclosestcluster will be modified to return nulls for 
            //clusters outside minimum radius, so this must handle nulls
            if(firstRun == true){
                linkedClusters.add(clusters.get(0));
                firstRun = false;
            }
            else {
                linkedClusters.add(getClosestCluster(clusters, primePosition));
            }
            
            double eventEnergy = 0;
            for(Cluster cluster : clusters){
                eventEnergy += cluster.getEnergy();
                aida.cloud1D("cluster energies").fill(cluster.getEnergy());
                aida.cloud1D(name+"cluster energies").fill(cluster.getEnergy());
            }
            //set to 10GeV particles for now...
            aida.cloud1D("Percent event energies detected").fill(eventEnergy/10);
            aida.cloud1D(name+"Percent event energies detected").fill(eventEnergy/10);
        }
            
        double linkedClusterEnergy = 0;
        for(Cluster cluster : linkedClusters){
            
            double clusterEnergy = 0;
            List<CalorimeterHit> calHits = cluster.getCalorimeterHits();
            
            for(CalorimeterHit hit : calHits){
                clusterEnergy += hit.getRawEnergy();
            }
            
            linkedClusterEnergy += clusterEnergy;
            aida.cloud1D("linkedClusters energy per cluster").fill(clusterEnergy);
            aida.cloud1D("linkedClusters total raw energies").fill(linkedClusterEnergy);
        }
        }
    }
    
    public void mipProcess(EventHeader event){
        
        double threshEM = 0.0047;
        double threshH = 0.00048;
        double threshMu = 0.02;
        double enEM = 0;
        double enH = 0;
        double enMu = 0;
        double en = 0;
        double enEMb = 0;
        double enHb = 0;
        double enMub = 0;
        
        List<SimCalorimeterHit> hitcolEM = 
                event.get(SimCalorimeterHit.class, "EcalBarrelHits");
        List<SimCalorimeterHit> hitcolH = 
                event.get(SimCalorimeterHit.class, "HcalBarrelHits");
        List<SimCalorimeterHit> hitcolMu = 
                event.get(SimCalorimeterHit.class, "MuonBarrelHits");
        
    
        for(SimCalorimeterHit hit : hitcolEM){
            en = hit.getRawEnergy();
            enEMb += en;
            mipEM.fill(en);
            if(en>threshEM){
                enEM += en;
            }
        }
        for(SimCalorimeterHit hit : hitcolH){
            en = hit.getRawEnergy();
            enHb += en;
            mipH.fill(en);
            if(en>threshH){
                enH += en;
            }
        }
        for(SimCalorimeterHit hit : hitcolMu){
            en = hit.getRawEnergy();
            enMub += en;
            mipMu.fill(en);
            if(en>threshMu){
                enMu += en;
            }
        }
        mipCutEM.fill(enEM);
        mipCutH.fill(enH);
        mipCutMu.fill(enMu);
        
        mipCutEMb.fill(enEMb);
        mipCutHb.fill(enHb);
        mipCutMub.fill(enMub);
    }
    
    public void mipCutProcessor(EventHeader event){
        
        double threshEM = 0.0047;
        double threshH = 0.00048;
        double threshMu = 0.02;
        
        List<SimCalorimeterHit> hitcolEM = 
                event.get(SimCalorimeterHit.class, "EcalBarrelHits");
        List<SimCalorimeterHit> hitcolH = 
                event.get(SimCalorimeterHit.class, "HcalBarrelHits");
        List<SimCalorimeterHit> hitcolMu = 
                event.get(SimCalorimeterHit.class, "MuonBarrelHits");
        
        
    }
    public void enResProc(List<SimCalorimeterHit> hitcol, String path){
        double en = 0;
        for(double i=0; i<1000; i+=5){
            for(SimCalorimeterHit hit : hitcol){
                if(getTPrime(hit)<i){
                    en += hit.getRawEnergy();
                }
            }
            aida.cloud1D("res/"+path).fill(i,en/10000);
            aida.cloud2D("res/"+path+"2d").fill(i,en/10000);
        }
    }
    /*
         * Commenting out this (functional) code while I use this class
    protected void process(EventHeader event){
        
        super.process(event);
        
         * to play wtih clusters so I can figure out a good way to 'join' clusters
        EVENTS++;
        
        List<List<SimCalorimeterHit>> hitcol = event.get(SimCalorimeterHit.class);
        
        double en = 0;
        //double time = 0;
        for(double i=0; i<1000; i+=5){
            for(List<SimCalorimeterHit> hits : hitcol){
                for(SimCalorimeterHit hit : hits){
                    if(getTPrime(hit)<i){
                        en+=hit.getRawEnergy();
                    }
                }
            }
            cutEnergies.fill(
                    i,en/10000);
            cutEnergies2D.fill(
                    i,en);
            en = 0;
        }
         
    }
     */
    protected void endOfData(){
        
        meanEnergies = hf.createHistogram1D(
                "meanEnergies",
                cutEnergies.axis().bins(),
                cutEnergies.axis().lowerEdge(),
                cutEnergies.axis().upperEdge());
        widthEnergies = hf.createHistogram1D(
                "widthEnergies",
                cutEnergies.axis().bins(),
                cutEnergies.axis().lowerEdge(),
                cutEnergies.axis().upperEdge());
        
        for(int i=0; i<cutEnergies2D.xAxis().bins(); i++){
            
            tempEnergies = hf.createHistogram1D(
                "tempEnergies",
                1,0,1);
            tempEnergies = hf.createHistogram1D(
                "tempEnergies",
                cutEnergies2D.yAxis().bins(),
                cutEnergies2D.yAxis().lowerEdge(),
                cutEnergies2D.yAxis().upperEdge());
             
            for(int j=0; j<cutEnergies2D.yAxis().bins(); j++){
                    tempEnergies.fill(
                            cutEnergies2D.yAxis().binCenter(j),
                            cutEnergies2D.binHeight(i,j));
            }
                
            meanEnergies.fill(
                    cutEnergies2D.xAxis().binCenter(i),
                    tempEnergies.mean());
            widthEnergies.fill(
                    cutEnergies2D.xAxis().binCenter(i),
                    tempEnergies.rms());            
        }
        
    }
    
    public double getRadius(double[] point){
         return Math.sqrt(point[0]*point[0]+point[1]*point[1]+point[2]*point[2]);
     }
     public double getTPrime(SimCalorimeterHit hit){
         return hit.getTime() - getRadius(hit.getPosition())/299.792458;
     }
}
