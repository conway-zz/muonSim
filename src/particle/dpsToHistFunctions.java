/*
 * This is for functions that create specific 
 * histograms from dataObj dps objects.
 * Will mostly call math functions from
 * analysisFunctions
 * 
 * @author Alex Conway
 */

import hep.aida.*;

public abstract class dpsToHistFunctions extends datasetPROCESS {
    
    public IHistogram1D graphTpEn(dataObj data, IHistogramFactory hf){
        IHistogram1D rv = hf.createHistogram1D(
                data.NAME, 
                data.NAME+"tpEn",
                data.hitsDPS.size(),
                data.hitsDPS.lowerExtent(5),
                data.hitsDPS.upperExtent(5));
        
        for(int i=0; i<data.hitsDPS.size(); i++){
            rv.fill(data.hitsDPS.point(i).coordinate(5).value(),
                    data.hitsDPS.point(i).coordinate(4).value());
        }        
        return rv;
    }
    
}
