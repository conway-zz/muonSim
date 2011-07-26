/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import hep.aida.*;
import java.io.*;
/**
 *
 * @author agias
 */
public class aidaProcessor {
    
    static IAnalysisFactory af;
    static ITree TREE;
    static IHistogramFactory hf;
    static IHistogram1D initial;
    static IHistogram1D fixed;
    
    public static void main(String[] args){
        af = IAnalysisFactory.create();
        TREE = null;
        try{
            TREE = af.createTreeFactory().create("tprimesen.xml/testTree", "xml", false, false);
        } 
        catch (IOException e) {
                System.out.println(e);
        }
        ITree srctree = (ITree) TREE.find("../testTree");
        initial = (IHistogram1D) srctree.find("Tprimes weighted by energy ");
        fixed = histFixer(initial);
    }
    
    public static IHistogram1D histFixer(IHistogram1D hist){
        int meanbin = hist.coordToIndex(hist.mean());
        int lowbin =0 ;
        double lowen =0 ;
        double sum = 0;
        boolean hityet = false;
        
        for(int i=meanbin; i>=0; i--){
            lowen +=hist.binHeight(i);
        }
        
        double highen = 0;
        for(int i=meanbin; i<hist.axis().bins(); i++){
            highen +=hist.binHeight(i);
        }
        System.out.println(meanbin+","+lowen+","+highen);
        for(int i=0; i<hist.axis().bins(); i++){
            if(!hityet){
                System.out.println(i+","+sum);
                sum += hist.binHeight(meanbin-i);
                if(sum>.99*lowen){
                    lowbin = (meanbin-i);
                    hityet = true;
                }
            }
        }
        System.out.println(lowbin);
        hityet = false;
        sum = 0;
        int highbin = 0;
        for(int i=1; i<hist.axis().bins(); i++){
            if(!hityet){
                System.out.println(i+","+sum);
                sum += hist.binHeight(meanbin+i);
                if(sum>.99*highen){
                    highbin = (meanbin+i);
                    hityet = true;
                }
            }
        }
        System.out.println(highbin);
        IHistogram1D rv = hf.createHistogram1D(
                "fixed "+hist.title(),
                "fixed "+hist.title(),
                highbin-lowbin, hist.axis().binCenter(lowbin),hist.axis().binCenter(highbin));
        for(int i=lowbin; i<highbin; i++){
            rv.fill(hist.axis().binCenter(i), hist.binHeight(i));
        }
        return rv;
    }
}
