/*
 * The actual Driver.
 * Use this to instantiate datsets,
 * run dataset.subProcess()
 * run this.postProcess()
 */
import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;
import hep.aida.*;


public class processor extends Driver {
    
    String[] calorimeters = { "EcalBarrelHits", "HcalBarrelHits" };
    IAnalysisFactory af;
    ITree TREE;
    IHistogramFactory hf;
    String cal;
    double EVENTS;
    double E_MC;
    double E_CAL;
    dataset EM;
    dataset H;
    dataset Mu;
    
    //TODO: auto-instantiate by string[] calorimeters
    //
    //@Override
    protected void startOfData(){
        af = IAnalysisFactory.create();
        TREE = af.createTreeFactory().createTree();
        hf = af.createHistogramFactory(TREE);
        
        EM = new dataset(calorimeters);
        
    }
    //@Override
    protected void process(EventHeader event){
        EM.subProcess(event);
    }
    
    //called when process(event) calls last
    @Override
    protected void endOfData(){
        postProcessor post = new postProcessor();
        post.integratedHist(EM.TPrimesEn,.95);
        post.windowEfficiency(EM.TPrimesEn);
        post.windowEfficiency(H.TPrimesEn);
        post.windowEfficiency(post.sumHist(EM.TPrimesEn, H.TPrimesEn));
    }    
}
