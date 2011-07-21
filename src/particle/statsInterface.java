/*
 * Common interface for the dataset processing functions.
 * Could have a better name =/
 */
import hep.aida.*;
import org.lcsim.event.EventHeader;
/**
 *
 * @author agias
 */
public interface statsInterface {
    
    
    //## aidaFunctions ##//
    
    //outputs exact integral of a histogram.
    public IHistogram1D integratedHist(IHistogram1D hist, IHistogramFactory hf,
            double maximum, double threshhold);
    
    //Theoretically creates CSV from histogram...
    public void histToCSV(IHistogram1D hist);
    
    //## datsetPROCESS FUNCTIONS
    
    //fill dataObj from all cals
    public void processGROUP(EventHeader event, dataObj data);
    
    //fill dataObj from selected cal
    public void processCAL(EventHeader event, dataObj data, String cal);
    
    //## analysisFunctions ##//
    /*
    public double getRadius(IDataPoint hit);
    
    public double getTPrime(IDataPoint hit);
     */
    
    //## dpsToHistFunctions ##//
    
    public IHistogram1D graphTpEn(dataObj data);
    
}
