package com.mobilecomputing.chaitanya.mcassignment3;

import android.util.Log;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;
import java.io.File;
import libsvm.LibSVM;

/**
 * Created by SAHIL on 03-04-2017.
 */

//references: https://github.com/AbeelLab/javaml
//references: http://www.csie.ntu.edu.tw/~cjlin/libsvm/
//references: https://sourceforge.net/p/java-ml/java-ml-code/ci/30237db0cb0457fe2629004ac91ac4cd2768d8ea/tree/src/tutorials/classification/TutorialLibSVM.java#l13

public class SVM1 {
    public void train(String filename) {

        try {
            Dataset dataset = FileHandler.loadDataset(new File(filename), 150, ",");
            Classifier svmClassifier = new LibSVM();
            svmClassifier.buildClassifier(dataset);

            Dataset dataForClassification = FileHandler.loadDataset(new File(filename), 150, ",");
            int correct = 0, wrong = 0;

            for (Instance inst : dataForClassification) {
                Object predictedClassValue = svmClassifier.classify(inst);
                Object realClassValue = inst.classValue();
                if (predictedClassValue.equals(realClassValue))
                    correct++;
                else
                    wrong++;
            }
            Log.d("Correct predictions  ", correct+"");
            Log.d("Wrong predictions ", wrong+"");
        }
        catch (Exception e) {
            Log.d("Error", e.getMessage());
        }

    }
}
