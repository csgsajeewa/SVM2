/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package svm2;


import libsvm.svm;
import libsvm.svm_model;
import weka.classifiers.functions.LibSVM;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;


/**
 *
 * @author hp
 */
public class SVMWithInternalMethods {
     public static void main(String[] args) {
        System.out.println("-------------------------------------------------");
        System.out.println("Running LIBSVM With Interanl Methods");
        System.out.println("---------------------------------------------------");
        
        SVMWithInternalMethods wekaTestDB = new SVMWithInternalMethods();
        try {
         
           wekaTestDB.testCrossValidataion();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void testCrossValidataion() throws Exception {

//      LibSVM --> initialize the model and set SVM type and kernal type
             
        LibSVMUpdated libSVM = new LibSVMUpdated();
        String svmOptions = "-S 0 -K 2 -C 8 -G 0.001953125 -W 10 1"; //-C 3 -G 0.001953125"
        libSVM.setOptions(weka.core.Utils.splitOptions(svmOptions));
        System.out.println("SVM Type and Keranl Type= " + libSVM.getSVMType() + libSVM.getKernelType());//1,3 best result 81%
       
        ////////////////////////////Normalization/////////////////
        libSVM.setNormalize(true);
        
//      load training data from .arff file
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("C:\\Users\\hp\\Desktop\\SVM implementation\\arffData\\trainingData.arff");
        System.out.println("\n\nLoaded data:\n\n" + source.getDataSet());
        Instances dataFiltered = source.getDataSet();
        dataFiltered.setClassIndex(0);
        libSVM.buildClassifier(dataFiltered);
        double[] prediction=libSVM.distributionForInstance(dataFiltered.instance(4));
        System.out.println(prediction[0]+"  "+prediction[1]);
        svm_model svmModel=libSVM.getSVMModel();
        int n[]=svmModel.sv_indices;
         
         for(int i=0;i<n.length;i++){
             System.out.println(n[i]);
         }
         System.out.println("Size="+n.length);
         
         svm.svm_save_model("C:\\Users\\hp\\Desktop\\SVM implementation\\arffData\\tt", svmModel);
         
         int otherCount=0;
         int crimeCount=0;
           for (int k = 0; k < n.length; k++) {
               Instance i=dataFiltered.instance(n[k]-1);
               System.out.println(n[k]-1+" "+i.classValue());
               if(i.classValue()==0.0)
                   crimeCount++;
               if(i.classValue()==1.0)
                   otherCount++;
        }
           System.out.println(crimeCount);
           System.out.println(otherCount);
        

    }

   

 

    
}
