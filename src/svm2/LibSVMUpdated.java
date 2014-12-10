/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package svm2;

import libsvm.svm;
import libsvm.svm_model;
import weka.classifiers.functions.LibSVM;


public class LibSVMUpdated extends LibSVM {

  
  public svm_model getSVMModel(){
      return (svm_model)m_Model;
  }
  
  public svm getSVM(){
      return new svm();
  }
}

