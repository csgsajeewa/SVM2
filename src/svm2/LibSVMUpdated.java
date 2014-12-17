package svm2;

import libsvm.svm;
import libsvm.svm_model;
import weka.classifiers.functions.LibSVM;

/**
 * LibSVM extension to access underlying model in order to analyze support
 * vectors
 *
 * @author chamath
 */
public class LibSVMUpdated extends LibSVM {

    /**
     * access underlying model
     *
     * @return
     */
    public svm_model getSVMModel() {
        return (svm_model) m_Model;
    }

    /**
     * access underlying svm classifier
     *
     * @return
     */
    public svm getSVM() {
        return new svm();
    }
}
