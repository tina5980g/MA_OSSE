import pandas as pd
from sklearn.model_selection import train_test_split
from catboost import CatBoostClassifier
from sklearn.metrics import roc_auc_score
from argparse import ArgumentParser
import numpy as np
import datetime

# This is taken (slightly adjusted) straight from https://github.com/desstaw/DataPrivacy_SimulatedAnnealing

def score_binary(model, x_test, y_test):
    # Predict on the testing data
    y_pred = model.predict(x_test)
    y_score = list(map(lambda predicted_value: predicted_value[0] == y_test.iloc[0], y_pred.tolist()))
    y_true = list(map(lambda predicted_value: predicted_value == y_test.iloc[0], y_test.tolist()))
    # Calculate the AUC-ROC score
    auc_roc = roc_auc_score(y_true, y_score)
    print("AUC-ROC Score (binary):", auc_roc)

    return auc_roc

def score_multiclass(model, x_test, y_test) :
    y_pred = model.predict_proba(x_test)

    # Calculate the AUC-ROC score
    # TODO: can we just use ovr here?
    classes = y_test.drop_duplicates()
    y_true = []
    for indexVal, val in enumerate(y_test):
        new_val = []
        for indexLabel, clazz in enumerate(classes):
            new_val.append(val == clazz)
        y_true.append(new_val)

    #sanity check, at least 3 different classes need to have been matched
    if len(classes) <= 2:
        raise Exception('test-sample too small')

    auc_roc = roc_auc_score(y_true, y_pred, multi_class="ovr")
    print("AUC-ROC Score (multiclass):", auc_roc)

    return auc_roc

def train_model(columns_source, column_target, df):
    x = df[columns_source]
    y = df[column_target]
    # Convert categorical features to strings
    categorical_features_indices = x.select_dtypes(include=['object']).columns
    print('categorical features', categorical_features_indices)

    random_seed = 32
    np.random.seed(random_seed)
    # Split the data into training and testing sets
    X_train, X_test, y_train, y_test = train_test_split(x, y, test_size=0.2, random_state=random_seed)

    # Initialize the CatBoostClassifier
    catboost_model = CatBoostClassifier(iterations=150, depth=7, learning_rate=0.08, cat_features=list(categorical_features_indices))

    # Train the model on the training data
    catboost_model.fit(X_train, y_train, cat_features=list(categorical_features_indices))

    if len(y.drop_duplicates()) > 2:
        # multiclass
        return score_multiclass(catboost_model, X_test, y_test)
    else:
        return score_binary(catboost_model, X_test, y_test)


parser = ArgumentParser()
parser.add_argument("-f", "--file", dest="filename", required=True)
parser.add_argument("-cs", "--columnsSource", dest="columnsSource", nargs='+', required=True)
parser.add_argument("-ct", "--columnTarget", dest="columnTarget", required=True)
parser.add_argument("-op", "--outputPath", dest="outputPath")
parser.add_argument("-oid", "--outputId", dest="outputId")

args = parser.parse_args()

rawdata = pd.read_csv(args.filename)
score = train_model(args.columnsSource, args.columnTarget, rawdata)

if args.outputId is not None:
    outputfile = ''
    if args.outputPath is not None:
        outputfile += args.outputPath + "/"
    outputfile += args.outputId
    with open(outputfile, "a") as myfile:
        myfile.write(str(datetime.datetime.now()) + ","+ str(score) + "\n")

