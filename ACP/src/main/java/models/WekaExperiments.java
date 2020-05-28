package models;

import builders.DecisionPathBuilder;
import models.features.features_vector.FeaturesVector;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.*;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;

public class WekaExperiments {
    /** file names are defined*/
    public static final String TRAINING_DATA_SHORT_SET_FILENAME="fv_train_sh.arff";
    public static final String TRAINING_DATA_SET_FILENAME="fv_train.arff";
    public static final String TESTING_DATA_SET_FILENAME="fv_test.arff";


    public static void main(String[] args) throws Exception {
        try {
            testModelShTree();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void trainModelShTree() throws Exception {
        Instances trainingDataSet = getDataSet(TRAINING_DATA_SHORT_SET_FILENAME);

        RandomTree tree = new RandomTree();
        tree.buildClassifier(trainingDataSet);
        System.out.println(tree.toString());

        //SerializationHelper.write("RTree-ACP-SH.model", tree);
    }

    public static void testModelShTree() throws Exception {
        Instances trainingDataSet = getDataSet(TRAINING_DATA_SHORT_SET_FILENAME);

        RandomTree tree = new RandomTree();
        tree.buildClassifier(trainingDataSet);
        System.out.println(tree.toString());

        //SerializationHelper.write("RTree-ACP-SH.model", tree);
    }

    public static void trainModelSh() throws Exception {
        RandomTree forest = (RandomTree) SerializationHelper.read("RTree-ACP-SH.model");
        Random r = new Random();

        try {
            String s = "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0.333333,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0.333333,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,51,17,1,0.333333,2,2,286764777,95588259,3,1642,40.04878,54,1.317073,0";
            String s1 = "0,0,0,0,0,0,0,0,0,0,0,0,5,0.277778,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0.055556,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0.055556,0,0,0,0,0,0,0,0,0,0,0,0,0,0,6,0.333333,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,969,53.833333,33,1.833333,5,3,86514241,41543694.222222,18,0,0,0,0,1";
            String[] p = s1.split(",");

            Instance newInstance  = new DenseInstance(118);
            for(int i = 0 ; i < 117 ; i++) {
                newInstance.setValue(i , Double.parseDouble(p[i]));
            }

            newInstance.setValue(117 , -1);

            ArrayList<Attribute> attributes = buildAttributes();
            Instances dataUnlabeled = new Instances("TestInstances", attributes, 0);
            dataUnlabeled.add(newInstance);
            dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);
            double pred = forest.classifyInstance(dataUnlabeled.firstInstance());
            //System.out.println(forest.getClassifier());
            System.out.println(forest);
            System.out.println(pred);
            String tree = forest.toString();
            //new DecisionPathBuilder(tree).buildPath(FeaturesVector.)
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testModel() throws Exception {
        RandomForest forest = (RandomForest) SerializationHelper.read("RF-ACP.model");
        Random r = new Random();

        try {
            String s = "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0.333333,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0.333333,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,51,17,1,0.333333,2,2,286764777,95588259,3,1642,40.04878,54,1.317073,0";
            String s1 = "0,0,0,0,0,0,0,0,0,0,0,0,5,0.277778,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0.055556,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0.055556,0,0,0,0,0,0,0,0,0,0,0,0,0,0,6,0.333333,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,969,53.833333,33,1.833333,5,3,86514241,41543694.222222,18,0,0,0,0,1";
            String[] p = s1.split(",");

            Instance newInstance  = new DenseInstance(118);
            for(int i = 0 ; i < 117 ; i++) {
                newInstance.setValue(i , Double.parseDouble(p[i]));
            }

            newInstance.setValue(117 , -1);

            ArrayList<Attribute> attributes = buildAttributes();
            Instances dataUnlabeled = new Instances("TestInstances", attributes, 0);
            dataUnlabeled.add(newInstance);
            dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);
            double pred = forest.classifyInstance(dataUnlabeled.firstInstance());
            System.out.println(forest.getClassifier());
            System.out.println(pred);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testModel2() throws Exception {
        RandomTree forest = (RandomTree) SerializationHelper.read("RTree-ACP.model");
        Random r = new Random();

        try {
            String s = "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0.333333,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0.333333,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,51,17,1,0.333333,2,2,286764777,95588259,3,1642,40.04878,54,1.317073,0";
            String s1 = "0,0,0,0,0,0,0,0,0,0,0,0,5,0.277778,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0.055556,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0.055556,0,0,0,0,0,0,0,0,0,0,0,0,0,0,6,0.333333,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,969,53.833333,33,1.833333,5,3,86514241,41543694.222222,18,0,0,0,0,1";
            String[] p = s1.split(",");

            Instance newInstance  = new DenseInstance(118);
            for(int i = 0 ; i < 117 ; i++) {
                newInstance.setValue(i , Double.parseDouble(p[i]));
            }

            newInstance.setValue(117 , -1);

            ArrayList<Attribute> attributes = buildAttributes();
            Instances dataUnlabeled = new Instances("TestInstances", attributes, 0);
            dataUnlabeled.add(newInstance);
            dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);
            double pred = forest.classifyInstance(dataUnlabeled.firstInstance());
            //System.out.println(forest.getClassifier());
            System.out.println(forest);
            System.out.println(pred);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testModelBoth() throws Exception {
        RandomTree rtree = (RandomTree) SerializationHelper.read("RTree-ACP.model");
        RandomForest forest = (RandomForest) SerializationHelper.read("RF-ACP.model");

        try {
            Instances testingDataSet = getDataSet(TESTING_DATA_SET_FILENAME);
            Enumeration<Instance> enumeration =  testingDataSet.enumerateInstances();
            while(enumeration.hasMoreElements()) {
                Instance instance = enumeration.nextElement();
                double real = instance.classValue();
                instance.setValue(117, -1);
                double p1 = forest.classifyInstance(instance);
                double p2 = rtree.classifyInstance(instance);
                System.out.print(real);
                System.out.print(" ");
                System.out.print(p1);
                System.out.print(" ");
                System.out.print(p2);
                System.out.print("\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trainModel() throws Exception {
        Instances trainingDataSet = getDataSet(TRAINING_DATA_SET_FILENAME);
        Instances testingDataSet = getDataSet(TESTING_DATA_SET_FILENAME);

        RandomForest forest = new RandomForest();
        forest.buildClassifier(trainingDataSet);

        SerializationHelper.write("RF-ACP.model", forest);
    }

    public static void trainModel2() throws Exception {
        Instances trainingDataSet = getDataSet(TRAINING_DATA_SET_FILENAME);

        RandomTree forest = new RandomTree();
        forest.buildClassifier(trainingDataSet);

        SerializationHelper.write("RTree-ACP.model", forest);
    }

    private static Instances getDataSet(String fileName) throws IOException {
        ArffLoader loader = new ArffLoader();
        loader.setFile(new File(fileName));
        Instances dataSet = loader.getDataSet();
        dataSet.setClassIndex(dataSet.numAttributes() - 1);
        return dataSet;
    }

    private static void printAttributes() throws IOException {
        Instances testingDataSet = getDataSet(TESTING_DATA_SET_FILENAME);

        System.out.println("ArrayList<Attribute> attributes = new ArrayList<Attribute>();");
        for (int i = 0; i < testingDataSet.numAttributes(); ++i) {
            Attribute attr = testingDataSet.attribute(i);
            System.out.println(String.format("attributes.add(new Attribute(\"%s\"));", attr.name()));
        }
    }

    private static ArrayList<Attribute> buildAttributes() {
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("TotalConnectivity"));
        attributes.add(new Attribute("TotalConnectivityPerLine"));
        attributes.add(new Attribute("FieldConnectivity"));
        attributes.add(new Attribute("FieldConnectivityPerLine"));
        attributes.add(new Attribute("MethodConnectivity"));
        attributes.add(new Attribute("MethodConnectivityPerLine"));
        attributes.add(new Attribute("KeywordAbstractTotalCount"));
        attributes.add(new Attribute("KeywordAbstractCountPerLine"));
        attributes.add(new Attribute("KeywordContinueTotalCount"));
        attributes.add(new Attribute("KeywordContinueCountPerLine"));
        attributes.add(new Attribute("KeywordForTotalCount"));
        attributes.add(new Attribute("KeywordForCountPerLine"));
        attributes.add(new Attribute("KeywordNewTotalCount"));
        attributes.add(new Attribute("KeywordNewCountPerLine"));
        attributes.add(new Attribute("KeywordSwitchTotalCount"));
        attributes.add(new Attribute("KeywordSwitchCountPerLine"));
        attributes.add(new Attribute("KeywordAssertTotalCount"));
        attributes.add(new Attribute("KeywordAssertCountPerLine"));
        attributes.add(new Attribute("KeywordDefaultTotalCount"));
        attributes.add(new Attribute("KeywordDefaultCountPerLine"));
        attributes.add(new Attribute("KeywordPackageTotalCount"));
        attributes.add(new Attribute("KeywordPackageCountPerLine"));
        attributes.add(new Attribute("KeywordSynchronizedTotalCount"));
        attributes.add(new Attribute("KeywordSynchronizedCountPerLine"));
        attributes.add(new Attribute("KeywordBooleanTotalCount"));
        attributes.add(new Attribute("KeywordBooleanCountPerLine"));
        attributes.add(new Attribute("KeywordDoTotalCount"));
        attributes.add(new Attribute("KeywordDoCountPerLine"));
        attributes.add(new Attribute("KeywordIfTotalCount"));
        attributes.add(new Attribute("KeywordIfCountPerLine"));
        attributes.add(new Attribute("KeywordPrivateTotalCount"));
        attributes.add(new Attribute("KeywordPrivateCountPerLine"));
        attributes.add(new Attribute("KeywordThisTotalCount"));
        attributes.add(new Attribute("KeywordThisCountPerLine"));
        attributes.add(new Attribute("KeywordBreakTotalCount"));
        attributes.add(new Attribute("KeywordBreakCountPerLine"));
        attributes.add(new Attribute("KeywordDoubleTotalCount"));
        attributes.add(new Attribute("KeywordDoubleCountPerLine"));
        attributes.add(new Attribute("KeywordImplementsTotalCount"));
        attributes.add(new Attribute("KeywordImplementsCountPerLine"));
        attributes.add(new Attribute("KeywordProtectedTotalCount"));
        attributes.add(new Attribute("KeywordProtectedCountPerLine"));
        attributes.add(new Attribute("KeywordThrowTotalCount"));
        attributes.add(new Attribute("KeywordThrowCountPerLine"));
        attributes.add(new Attribute("KeywordByteTotalCount"));
        attributes.add(new Attribute("KeywordByteCountPerLine"));
        attributes.add(new Attribute("KeywordElseTotalCount"));
        attributes.add(new Attribute("KeywordElseCountPerLine"));
        attributes.add(new Attribute("KeywordImportTotalCount"));
        attributes.add(new Attribute("KeywordImportCountPerLine"));
        attributes.add(new Attribute("KeywordPublicTotalCount"));
        attributes.add(new Attribute("KeywordPublicCountPerLine"));
        attributes.add(new Attribute("KeywordThrowsTotalCount"));
        attributes.add(new Attribute("KeywordThrowsCountPerLine"));
        attributes.add(new Attribute("KeywordCaseTotalCount"));
        attributes.add(new Attribute("KeywordCaseCountPerLine"));
        attributes.add(new Attribute("KeywordEnumTotalCount"));
        attributes.add(new Attribute("KeywordEnumCountPerLine"));
        attributes.add(new Attribute("KeywordInstanceofTotalCount"));
        attributes.add(new Attribute("KeywordInstanceofCountPerLine"));
        attributes.add(new Attribute("KeywordReturnTotalCount"));
        attributes.add(new Attribute("KeywordReturnCountPerLine"));
        attributes.add(new Attribute("KeywordTransientTotalCount"));
        attributes.add(new Attribute("KeywordTransientCountPerLine"));
        attributes.add(new Attribute("KeywordCatchTotalCount"));
        attributes.add(new Attribute("KeywordCatchCountPerLine"));
        attributes.add(new Attribute("KeywordExtendsTotalCount"));
        attributes.add(new Attribute("KeywordExtendsCountPerLine"));
        attributes.add(new Attribute("KeywordIntTotalCount"));
        attributes.add(new Attribute("KeywordIntCountPerLine"));
        attributes.add(new Attribute("KeywordShortTotalCount"));
        attributes.add(new Attribute("KeywordShortCountPerLine"));
        attributes.add(new Attribute("KeywordTryTotalCount"));
        attributes.add(new Attribute("KeywordTryCountPerLine"));
        attributes.add(new Attribute("KeywordCharTotalCount"));
        attributes.add(new Attribute("KeywordCharCountPerLine"));
        attributes.add(new Attribute("KeywordFinalTotalCount"));
        attributes.add(new Attribute("KeywordFinalCountPerLine"));
        attributes.add(new Attribute("KeywordInterfaceTotalCount"));
        attributes.add(new Attribute("KeywordInterfaceCountPerLine"));
        attributes.add(new Attribute("KeywordStaticTotalCount"));
        attributes.add(new Attribute("KeywordStaticCountPerLine"));
        attributes.add(new Attribute("KeywordVoidTotalCount"));
        attributes.add(new Attribute("KeywordVoidCountPerLine"));
        attributes.add(new Attribute("KeywordClassTotalCount"));
        attributes.add(new Attribute("KeywordClassCountPerLine"));
        attributes.add(new Attribute("KeywordFinallyTotalCount"));
        attributes.add(new Attribute("KeywordFinallyCountPerLine"));
        attributes.add(new Attribute("KeywordLongTotalCount"));
        attributes.add(new Attribute("KeywordLongCountPerLine"));
        attributes.add(new Attribute("KeywordStrictfpTotalCount"));
        attributes.add(new Attribute("KeywordStrictfpCountPerLine"));
        attributes.add(new Attribute("KeywordVolatileTotalCount"));
        attributes.add(new Attribute("KeywordVolatileCountPerLine"));
        attributes.add(new Attribute("KeywordConstTotalCount"));
        attributes.add(new Attribute("KeywordConstCountPerLine"));
        attributes.add(new Attribute("KeywordFloatTotalCount"));
        attributes.add(new Attribute("KeywordFloatCountPerLine"));
        attributes.add(new Attribute("KeywordNativeTotalCount"));
        attributes.add(new Attribute("KeywordNativeCountPerLine"));
        attributes.add(new Attribute("KeywordSuperTotalCount"));
        attributes.add(new Attribute("KeywordSuperCountPerLine"));
        attributes.add(new Attribute("KeywordWhileTotalCount"));
        attributes.add(new Attribute("KeywordWhileCountPerLine"));
        attributes.add(new Attribute("TotalSymbolsInCodeFragment"));
        attributes.add(new Attribute("AverageSymbolsInCodeLine"));
        attributes.add(new Attribute("TotalLinesDepth"));
        attributes.add(new Attribute("AverageLinesDepth"));
        attributes.add(new Attribute("TotalCommitsInFragment"));
        attributes.add(new Attribute("TotalAuthorsInFragment"));
        attributes.add(new Attribute("LiveTimeOfFragment"));
        attributes.add(new Attribute("AverageLiveTimeOfLine"));
        attributes.add(new Attribute("TotalLinesOfCode"));
        attributes.add(new Attribute("MethodDeclarationSymbols"));
        attributes.add(new Attribute("MethodDeclarationAverageSymbols"));
        attributes.add(new Attribute("MethodDeclarationDepth"));
        attributes.add(new Attribute("MethodDeclarationDepthPerLine"));
        attributes.add(new Attribute("label"));
        return attributes;
    }
}
