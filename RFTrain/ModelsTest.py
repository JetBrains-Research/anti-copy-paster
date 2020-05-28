import pandas as pd
from sklearn.ensemble import RandomForestClassifier
import numpy as np
from sklearn.model_selection import train_test_split
import matplotlib.pyplot as plt

from  sklearn.metrics import recall_score, precision_score, f1_score
from sklearn.metrics import average_precision_score
from sklearn.naive_bayes import GaussianNB, BernoulliNB, MultinomialNB
from sklearn import svm
from sklearn.neural_network import MLPClassifier
from scipy.stats import wilcoxon
import statistics
from statistics import mean
from sklearn.metrics import matthews_corrcoef

def important_features(bayes):
    neg_class_prob_sorted = bayes.feature_log_prob_[0, :].argsort()
    pos_class_prob_sorted = bayes.feature_log_prob_[1, :].argsort()

    print(neg_class_prob_sorted[:10])
    print(pos_class_prob_sorted[:10])

def important_features_rf(rf):
    importances = rf.feature_importances_
    indices = np.argsort(importances)[-30:]
    print(np.argsort(importances)[-7:])
    plt.title('Feature Importances')
    plt.barh(range(len(indices)), importances[indices], color='b', align='center')
    plt.xlabel('Relative Importance')
    plt.savefig("res.png")

def test_bernoulli():
    print("\nBERNOULLI_NB")
    model = BernoulliNB(alpha=1)  # GaussianNB()
    test_model(model)
    important_features(model)

def test_gauss():
    print("\nGAUSS_NB")
    model = GaussianNB()
    test_model(model)

def test_SVM():
    print("\nSVM")
    model = svm.SVC(gamma='scale')
    test_model(model)

def test_RF():
    print("\nRF")
    model = RandomForestClassifier(n_estimators=1000, random_state=42)
    test_model(model)
    important_features_rf(model)

def test_NN():
    print("\nNN")
    model = MLPClassifier()
    test_model(model)

def test_model(model):
    features = pd.read_csv('res.csv')
    for i in range(200):
        features = features.sample(frac=1)
    labels = np.array(features['label'])
    features = features.drop('label', axis=1)
    features = features.drop('107', axis=1)

    feature_list = list(features.columns)
    features = np.array(features)
    train_features, test_features, train_labels, test_labels = train_test_split(features, labels, test_size=0.2,
                                                                                random_state=42)
    model.fit(train_features, train_labels)

    predictions = model.predict(test_features)
    errors = abs(predictions - test_labels)
    accuracy = 100 - 100 * (sum(errors) / len(test_labels))
    print('Accuracy:', round(accuracy, 2), '%')

    aps = average_precision_score(test_labels, predictions)
    print("Average precision score: " + str(aps))

    prec = precision_score(test_labels, predictions)
    print("Precision: " + str(prec))

    rec = recall_score(test_labels, predictions)
    print("Recall: " + str(rec))

    f1 = f1_score(test_labels, predictions)
    print("F1: " + str(f1))

    sr, pv = wilcoxon(predictions, test_labels)
    print("P-Value: " + str(pv))

    mcc = matthews_corrcoef(test_labels, predictions)
    print("MCC: " + str(mcc))

def test_RF_CV():
    print("\nRF")
    model = RandomForestClassifier(n_estimators=1000, random_state=42)
    test_modelCV(model)
    important_features_rf(model)

def test_modelCV(model):
    features = pd.read_csv('nonequal.csv')
    for i in range(200):
        features = features.sample(frac=1)

    labels = np.array(features['label'])
    labels = list(map(int, labels))
    print(len(labels))
    features = features.drop('label', axis=1)
    features = features.drop('107', axis=1)

    acc_ = []
    aps_ = []
    prec_ = []
    rec_ = []
    f1_ = []
    pv_ = []
    mcc_ = []

    for i in range(10):

        features = np.array(features)
        train_features, test_features, train_labels, test_labels = train_test_split(features, labels, test_size=0.1,
                                                                                random_state=(i+1) * 7)
        model.fit(train_features, train_labels)

        predictions = model.predict(test_features)
        errors = abs(predictions - test_labels)
        accuracy = 100 - 100 * (sum(errors) / len(test_labels))
        print('Accuracy:', round(accuracy, 2), '%')

        aps = average_precision_score(test_labels, predictions)
        print("Average precision score: " + str(aps))

        prec = precision_score(test_labels, predictions)
        print("Precision: " + str(prec))

        rec = recall_score(test_labels, predictions)
        print("Recall: " + str(rec))

        f1 = f1_score(test_labels, predictions)
        print("F1: " + str(f1))

        sr, pv = wilcoxon(predictions, test_labels)
        print("P-Value: " + str(pv))

        mcc = matthews_corrcoef(test_labels, predictions)
        print("MCC: " + str(mcc))

        acc_.append(accuracy)
        aps_.append(aps)
        prec_.append(prec)
        rec_.append(rec)
        f1_.append(f1)
        pv_.append(pv)
        mcc_.append(mcc)

    print()
    print("Mean acc: " + str(mean(acc_)))
    print("Mean aps: " + str(mean(aps_)))
    print("Mean prec: " + str(mean(aps_)))
    print("Mean rec: " + str(mean(rec_)))
    print("Mean f1: " + str(mean(f1_)))
    print("Mean p-val: " + str(mean(pv_)))
    print("Mean MCC: " + str(mean(mcc_)))

def test_model_real(model):
    train_features = pd.read_csv('false_train.csv')
    for i in range(200):
        train_features = train_features.sample(frac=1)
    train_labels = np.array(train_features['label'])
    train_features = train_features.drop('label', axis=1)
    train_features = train_features.drop('107', axis=1)
    train_features = np.array(train_features)

    test_features = pd.read_csv('false_test.csv')
    for i in range(200):
        test_features = test_features.sample(frac=1)
    test_labels = np.array(test_features['label'])
    test_features = test_features.drop('label', axis=1)
    test_features = test_features.drop('107', axis=1)
    test_features = np.array(test_features)

    model.fit(train_features, train_labels)

    predictions = model.predict(test_features)
    errors = abs(predictions - test_labels)
    accuracy = 100 - 100 * (sum(errors) / len(test_labels))
    print('Accuracy:', round(accuracy, 2), '%')

    aps = average_precision_score(test_labels, predictions)
    print("Average precision score: " + str(aps))

    prec = precision_score(test_labels, predictions)
    print("Precision: " + str(prec))

    rec = recall_score(test_labels, predictions)
    print("Recall: " + str(rec))

    f1 = f1_score(test_labels, predictions)
    print("F1: " + str(f1))

    sr, pv = wilcoxon(predictions, test_labels)
    print("P-Value: " + str(pv))

    mcc = matthews_corrcoef(test_labels, predictions)
    print("MCC: " + str(mcc))

def test_RF_real():
    print("\nRF")
    model = RandomForestClassifier(n_estimators=1000, random_state=42)
    test_model_real(model)
    important_features_rf(model)

if __name__ == "__main__":
    #test_bernoulli()
    #test_gauss()
    #test_SVM()
    #test_RF()
    #test_NN()
    test_RF_CV()
    #test_RF_real()