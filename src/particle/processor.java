/*
 * runs processor when called by JAS3.
 * Run runOnce() first
 * uncomment separate processXX(event) methods to use them.
 */
import org.lcsim.event.EventHeader;
import hep.aida.*;


public class processor extends aidaFunctions {
    
    String[] names;
    dataObj GROUP;
    //dataObj EM;
    //dataObj H;
    boolean hasRun = false;   
    
    IAnalysisFactory af;
    ITree TREE;
    IHistogramFactory hf;
    IHistogram1D tpEn;
    IDataPointSetFactory dpsf;
    
    //TODO: find a way to autoinstantiate, perhaps?
    /*public processor(String[] list){
        names = list;
        GROUP = new dataObj("GROUP");
        EM = new dataObj("EM");
        H = new dataObj("H");
    }
     */
    
    //TODO: instantiate  statsObj for each name in names
    protected void startOfData(){
        af = IAnalysisFactory.create();
        TREE = af.createTreeFactory().createTree();
        hf = af.createHistogramFactory(TREE);
        dpsf = af.createDataPointSetFactory(TREE);
        GROUP = new dataObj();
        //EM = new dataObj();
        //H = new dataObj();
        GROUP.hitsDPS =  dpsf.create("hitData","t,x,y,z,E,tp",6);
        //EM.hitsDPS =  dpsf.create("hitData","t,x,y,z,E,tp",6);
        //H.hitsDPS =  dpsf.create("hitData","t,x,y,z,E,tp",6);
    }
    
    protected void process(EventHeader event){
        
        processGROUP(event, GROUP);
       // processCAL(event, EM, "EcalBarrelHits");
       // processCAL(event, EM, "HcalBarrelHits");
    }
    
    //called when process(event) calls last
    protected void endOfData(){
        graphTpEn(GROUP, hf);
        //graphTpEn(EM, hf);
        //graphTpEn(H, hf);
    }
    
    
}
