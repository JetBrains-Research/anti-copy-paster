package models.offline;

import models.IPredictionModel;
import models.features.features_vector.IFeaturesVector;
import weka.classifiers.trees.RandomForest;
import weka.core.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class WekaBasedModel implements IPredictionModel {
    private RandomForest model;
    private ArrayList<Attribute> attributes;

    public WekaBasedModel() throws Exception {
        RandomForest forest = (RandomForest) SerializationHelper.read(getClass().getClassLoader().getResourceAsStream("RF-ACP-SH.model"));

        this.model = forest;
        this.attributes = buildAttributes();
    }

    public WekaBasedModel(String modelPath) throws Exception {
        RandomForest forest = (RandomForest) SerializationHelper.read(modelPath);

        this.model = forest;
        this.attributes = buildAttributes();
    }

    @Override
    public List<Integer> predict(List<IFeaturesVector> batch) throws Exception {
        Instances dataUnlabeled = new Instances("TestInstances", attributes, batch.size());

        for (IFeaturesVector vec: batch) {
            Instance item  = new DenseInstance(attributes.size());
            List<Float> values = vec.buildVector();

            for(int i = 0 ; i < values.size() ; i++) {
                item.setValue(i , (double)values.get(i));
            }

            item.setValue(attributes.size() - 1, -1);
            dataUnlabeled.add(item);
        }

        dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);

        List<Integer> results = new ArrayList<>();

        Enumeration<Instance> iterable = dataUnlabeled.enumerateInstances();

        while (iterable.hasMoreElements()) {
            Instance item = iterable.nextElement();
            double pred = model.classifyInstance(item);
            boolean predicted = pred > 0.8;
            results.add(predicted ? 1 : 0);
            //System.out.print("PREDICTION ");
            //System.out.print(predicted);
            //System.out.print(" ");
            //System.out.println(pred);
        }


        return results;
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
