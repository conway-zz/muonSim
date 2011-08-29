/**
 * Adapts code from Norman Graf's ClusterFinding class
 * and combines with energyResolution
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
import hep.aida.*;

public class nnClusterResolution extends Driver {
    
    private NearestNeighborClusterer _clusterer;
    private String[] _collectionNames;
    private AIDA aida = AIDA.defaultInstance();
    
    IAnalysisFactory af;
    ITree TREE;
    IHistogramFactory hf;
    //IHistogram1D totalEnergies;
    IHistogram1D cutEnergies;
    IHistogram2D cutEnergies2D;
    IHistogram1D meanEnergies;
    IHistogram1D widthEnergies;
    IHistogram1D tempEnergies;
    int bins;
    double min;
    double max;
    
    
    public nnClusterResolution()
    {
        int minCells = 35;
        int dU = 3;
        int dV = 3;
        int dLayer = 3;
        double threshold = 0.0001;
        _clusterer = new NearestNeighborClusterer(dU, dV, dLayer, minCells, threshold);
    }
    
    @Override
    protected void startOfData(){
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
        
        //CalHit list to process with energyResolution stuff
        List<CalorimeterHit> firstCluster = clusters.get(0).getCalorimeterHits();
        
        double en = 0;
        //double time = 0;
        for(double i=0; i<1000; i+=5){
            for(CalorimeterHit hit : firstCluster){
                if(getTPrime(hit)<i){
                    en+=hit.getRawEnergy();
                }
            }
            
            cutEnergies.fill(
                    i,en);
            cutEnergies2D.fill(
                    i,en);
            en = 0;
        }
    }
    
    @Override
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
     public double getTPrime(CalorimeterHit hit){
         return hit.getTime() - getRadius(hit.getPosition())/299.792458;
     }
     
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
