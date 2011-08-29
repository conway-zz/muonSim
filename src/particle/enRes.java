/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import org.lcsim.recon.cluster.nn.NearestNeighborClusterer;
import org.lcsim.recon.cluster.nn.NearestNeighborCluster;
import org.lcsim.recon.cluster.fixedcone.FixedConeClusterer;
import org.lcsim.recon.cluster.fixedcone.FixedConeClusterer.FixedConeDistanceMetric;
import org.lcsim.util.aida.AIDA;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//


/**
 *
 * @author agias
 */
public class enRes extends Driver {
    
    private FixedConeClusterer _fcclusterer;
    private NearestNeighborClusterer _nnclusterer;
    /*
    private String[] hitcollnames = {
            "EcalBarrelHits",
            "EcalEndcapHits",
            "HcalBarrelHits",
            "HcalEndcapHits",
            "MuonBarrelHits",
            "MuonEndcapHits"};
    */
    String[] hitcollnames = null;
    private double[] THRESH = {0.0047,0.0047,
        0.0048,0.0048,
        0.125,0.125};
    private AIDA aida;
    private ITree tree;
    
    
    IAnalysisFactory af = IAnalysisFactory.create();
    ITree Tree = af.createTreeFactory().create();
    IHistogramFactory hf = af.createHistogramFactory(Tree);
    
    IHistogram1D mip10;
    IHistogram1D mip100;
    IHistogram1D mip1000;
    
    IHistogram1D fcc10;
    IHistogram1D fcc100;
    IHistogram1D fcc1000;
    
    IHistogram1D nnc10;
    IHistogram1D nnc100;
    IHistogram1D nnc1000;

    
    public enRes() {
        
        double radius = .4;
        double seed = 0.;
        double minE = 0.;
        FixedConeDistanceMetric dm = FixedConeDistanceMetric.DOTPRODUCT;
        _fcclusterer = new FixedConeClusterer(radius, seed, minE, dm);
        
        int minCells = 35;
        int dU = 3;
        int dV = 3;
        int dLayer = 3;
        double threshold = 0.0001;
        _nnclusterer = new NearestNeighborClusterer(minCells,dU,dV,dLayer,threshold);
    }
    @Override
    protected void startOfData(){
        aida = AIDA.defaultInstance();
        tree = aida.tree();
        tree.mkdir("fcclusters/");
        tree.mkdir("ncclusters/");
        mip10 = hf.createHistogram1D("mip cut 10",
                200, 0, 10);
        mip100 = hf.createHistogram1D("mip cut 100",
                200, 0, 10);
        mip1000 = hf.createHistogram1D("mip cut 1000",
                200, 0, 10);
        
        fcc10 = hf.createHistogram1D("fcc cut 10",
                200, 0, 10);
        fcc100 = hf.createHistogram1D("fcc cut 100",
                200, 0, 10);
        fcc1000 = hf.createHistogram1D("fcc cut 1000",
                200, 0, 10);
        
        nnc10 = hf.createHistogram1D("nnc cut 10",
                200, 0, 10);
        nnc100 = hf.createHistogram1D("nnc cut 100",
                200, 0, 10);
        nnc1000 = hf.createHistogram1D("nnc cut 1000",
                200, 0, 10);
    }
    @Override
    protected void process(EventHeader event){
        
        //mipProcess(event);
        
        Map<Long, CalorimeterHit> hitmap = mapHits(event);
        List<Cluster> fcclusters = _fcclusterer.createClusters(hitmap);
        List<Cluster> nnclusters = _nnclusterer.createClusters(hitmap);
        
        //List<CalorimeterHit> hits = new ArrayList<CalorimeterHit>();
        
        
        if(fcclusters.size()>0){
            event.put("FCC", fcclusters);
            //aida.cloud1D("fcclusters/sizes").fill(fcclusters.size());
            Collections.sort(fcclusters, new ClusterSortByEnergy());
            processClusters(fcclusters, "fcclusters", true);
        }
        if(nnclusters.size()>0){
            event.put("NNC", nnclusters);
            //aida.cloud1D("nnclusters/sizes").fill(nnclusters.size());
            Collections.sort(nnclusters, new ClusterSortByEnergy());
            processClusters(nnclusters, "nnclusters", false);  
        }     
        /*
        double en = 0;
        for(int i=0; i<100; i++){
            for(CalorimeterHit hit : hits){            
                if(getTPrime(hit)<i){
                    en+=hit.getRawEnergy();
                }
            }
            aida.cloud1D("mip").fill(i,en/10000);
            aida.cloud2D("mip2D").fill(i,en);
            en = 0;
        }
         * 
         */
    }
    
    public void mipProcess(EventHeader event){
        
        List<List<SimCalorimeterHit>> hitcol = event.get(SimCalorimeterHit.class);
        List<SimCalorimeterHit> miphits = new ArrayList<SimCalorimeterHit>();
        for(List<SimCalorimeterHit> hits : hitcol){
            for(SimCalorimeterHit hit : hits){
                if(hit.getRawEnergy()>0.0048){
                    miphits.add(hit);
                }
            }
        }
        double en10=0;
        double en100=0;
        double en1000=0;
        for(CalorimeterHit hit : miphits){
            if(getTPrime(hit)<10){
                en10+=hit.getRawEnergy();                
            }
            if(getTPrime(hit)<100){
                en100+=hit.getRawEnergy();                
            }
            if(getTPrime(hit)<1000){
                en1000+=hit.getRawEnergy();                
            }
            //aida.cloud1D("mip").fill(i,en/10000);
            //aida.cloud2D("mip2D").fill(i,en);
        }
        mip10.fill(en10);
        mip100.fill(en100);
        mip1000.fill(en1000);
        en10=0;
        en100=0;
        en1000=0;
    }
    
    public void processClusters(List<Cluster> clusters, String name, boolean fcc){
        Cluster cluster = clusters.get(0);
        double en10=0;
        double en100=0;
        double en1000=0;
        for(CalorimeterHit hit : cluster.getCalorimeterHits()){
            if(getTPrime(hit)<10){
                en10+=hit.getRawEnergy();                
            }
            if(getTPrime(hit)<100){
                en100+=hit.getRawEnergy();                
            }
            if(getTPrime(hit)<1000){
                en1000+=hit.getRawEnergy();                
            }
            //aida.cloud1D("mip").fill(i,en/10000);
            //aida.cloud2D("mip2D").fill(i,en);
        }
        if(fcc){ 
            fcc10.fill(en10);
            fcc100.fill(en100);
            fcc1000.fill(en1000);
        }
        if(!fcc){
            nnc10.fill(en10);
            nnc100.fill(en100);
            nnc1000.fill(en1000);
        }
        
        //clusterResolution(cluster, name);
        /*
        int count = 0;
        for (Cluster cluster : clusters)
            {
         * 
         */
        /*
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
                    aida.cloud1D(name + "/energy first cluster").fill(e1);
                    aida.cloud1D(name + "/size first cluster").fill(e1);
                    //pos1 = cluster.getPosition();
                    clusterResolution(cluster, name);
                }
                
                if (count == 2)
                {
                    e2 = cluster.getEnergy();
                    aida.cloud1D(name + "/energy second cluster").fill(cluster.getEnergy());
                    aida.cloud1D(name + "/size second cluster").fill(cluster.getSize());
                    pos2 = cluster.getPosition();
                    pos3[0] = pos2[0] - pos1[0];
                    pos3[1] = pos2[1] - pos1[1];
                    pos3[2] = pos2[2] - pos1[2];
                    double delta = Math.sqrt(pos3[0] * pos3[0] + pos3[1] * pos3[1] + pos3[2] * pos3[2]);
                    aida.cloud1D(name + "/distance").fill(delta);
                    aida.cloud1D(name + "/deltaE").fill(e1 - e2);
                    aida.cloud2D(name + "/disvsdeltaE").fill(delta, e1 - e2);
                }
                
                if (count == 3)
                {
                    aida.cloud1D(name + "/energy third cluster").fill(cluster.getEnergy());
                }
                if (count > 3)
                {
                    aida.cloud1D(name + "/energy fourth and more cluster").fill(cluster.getEnergy());
                }

//
// Histogram the "corrected" energy
//
                aida.cloud1D(name + "/energy").fill(cluster.getEnergy());
                aida.cloud1D(name + "/size").fill(cluster.getSize());
                aida.cloud2D(name + "/sizevsenergy").fill(cluster.getSize(), cluster.getEnergy());
//
// Histogram the position as R vs Z
//
                double[] pos = cluster.getPosition();
                double R = Math.sqrt(pos[0] * pos[0] + pos[1] * pos[1]);
                aida.cloud2D(name + "/Position:R vs Z").fill(pos[2], R);
//
// Histogram the computed direction
//
                aida.cloud1D(name + "/Direction: theta").fill(cluster.getITheta());
                aida.cloud1D(name + "/Direction: phi").fill(cluster.getIPhi());
//
// Histogram the difference in direction and position theta,phi 
//
                double posphi = Math.atan2(pos[1], pos[0]);
                aida.cloud1D(name + "/delta phi").fill(posphi - cluster.getIPhi());
                double postheta = Math.PI / 2. - Math.atan2(pos[2], R);
                aida.cloud1D(name + "/delta theta").fill(postheta - cluster.getITheta());
                */
            
        
    }
    
    public void clusterResolution(Cluster cluster, String name){
        double en = 0;
        List<CalorimeterHit> hits = cluster.getCalorimeterHits();
        for(int i=0; i<100; i++){
            for(CalorimeterHit hit : hits){            
                if(getTPrime(hit)<i){
                    en+=hit.getRawEnergy();
                }
            }
            aida.cloud1D(name+"/energies in clusters").fill(i,en/10000);
            aida.cloud2D(name+"/energies2D in clusters").fill(i,en);
            en = 0;
        }
    }
    

    
    public Map<Long, CalorimeterHit> mapHits(EventHeader event){
    // which calorimeter hits should we cluster?
        Map<Long, CalorimeterHit> hitmap = new HashMap<Long, CalorimeterHit>();
        // which collections should we process?
        if (hitcollnames != null)
        {
            // only process the collections we requested
            for (int i = 0; i < hitcollnames.length; ++i)
            {
                List<SimCalorimeterHit> hits = event.getSimCalorimeterHits(hitcollnames[i]);
                double calEn = 0;
                for (SimCalorimeterHit h : hits)
                {
                    if(h.getRawEnergy()>THRESH[i]){
                        hitmap.put(h.getCellID(), h);
                        calEn+=h.getRawEnergy();
                    }
                    
                    // apply any time or energy cuts here...
                    
                }
                aida.cloud1D("contrib "+ hitcollnames[i]).fill(calEn);
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
                    //if(h.getRawEnergy()<THRESH[i]){
                        hitmap.put(h.getCellID(), h);
                    //}
                }
            }
        }
        return hitmap;
    }
    /*
     * TODO: This
     
    @Override
    protected void endOfData(){
        double upperEdge = 
                    aida.cloud1D("fcclusters/energies in clusters").upperEdge();
        double lowerEdge = 
                    aida.cloud1D("fcclusters/energies in clusters").lowerEdge();
        
        double lowerEdgeX =
                aida.cloud2D("fcclusters/energies2D in clusters").lowerEdgeX();
        double lowerEdgeY =
                aida.cloud2D("fcclusters/energies2D in clusters").lowerEdgeY();
        double upperEdgeX =
                aida.cloud2D("fcclusters/energies2D in clusters").upperEdgeX();
        double upperEdgeY =
                aida.cloud2D("fcclusters/energies2D in clusters").upperEdgeY();
        
        aida.cloud1D("fcclusters/energies in clusters").convert(
                200,lowerEdge,upperEdge);
        aida.cloud2D("fcclusters/energies2D in clusters").convert(
                200,lowerEdgeX,upperEdgeX,200,lowerEdgeY,upperEdgeY);
        
        IHistogram1D fcc1D = aida.cloud1D("fcclusters/energies in clusters").histogram();
        IHistogram2D fcc2D = aida.cloud2D("fcclusters/energies2D in clusters").histogram();
        
        for(int i=0; i<fcc2D.XAxis())
        
    }
    
     * 
     */
    
    public double getRadius(double[] point){
         return Math.sqrt(point[0]*point[0]+point[1]*point[1]+point[2]*point[2]);
     }
     public double getTPrime(CalorimeterHit hit){
         return hit.getTime() - getRadius(hit.getPosition())/299.792458;
     }
    
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
    
}
