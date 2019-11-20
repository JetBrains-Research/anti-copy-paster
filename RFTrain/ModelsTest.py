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

if __name__ == "__main__":
    #test_bernoulli()
    #test_gauss()
    #test_SVM()
    test_RF()
    #test_NN()