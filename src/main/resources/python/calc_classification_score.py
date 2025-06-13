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
    classes = y_test.drop_duplicates()

    #sanity check, at least 3 different classes need to have been matched
    if len(classes) <= 2:
        print('Test-sample is too small to calculate meaningful score')
        return -float('inf')

    y_pred = model.predict_proba(x_test)

    # Calculate the AUC-ROC score
    y_true = []
    for indexVal, val in enumerate(y_test):
        new_val = []
        for indexLabel, clazz in enumerate(classes):
            new_val.append(val == clazz)
        y_true.append(new_val)

    auc_roc = roc_auc_score(y_true, y_pred, multi_class="ovr")
    print("AUC-ROC Score (multiclass):", auc_roc)

    return auc_roc

def objective_function(df, solution, column_target, k, max_suppressed_fraction):
    grouped = df.groupby(solution)

    # Calculate the percentage of suppressed rows -- utility
    total_rows = len(df)
    suppressed_rows = sum(len(group) for group_name, group in grouped if len(group) < k)
    suppressed_fraction = suppressed_rows / total_rows
    print('SUPPRESSION FRACTION', suppressed_fraction)
    #print('Total Suppressed Rows:', suppressed_rows)
    print('Max Suppressed Fraction:', max_suppressed_fraction)


    suppressed_fraction = round(suppressed_fraction, 2)
    if suppressed_fraction <= max_suppressed_fraction: #+ 1e-6:

        # Filter the original dataframe to remove suppressed rows
        filtered_df = df[df.index.isin([idx for group_name, group in grouped if len(group) < k for idx in group.index])]
        print('FILTERED DF INFO', filtered_df.info())

        # Calculate and return classification accuracy as the objective function score
        classification_accuracy = calculate_classification_accuracy(solution, column_target, filtered_df)
        print('Classification accuracy (AUC-ROC)', classification_accuracy)
        return classification_accuracy

    else: #suppressed_fraction > max_suppressed_fraction:
        # If the constraint is violated, return a penalty (negative infinity)
        print('SUPPRESSION EXCEEDED LIMIT')
        return -float('inf')

def calculate_classification_accuracy(columns_source, column_target, df):
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
parser.add_argument("-k", dest="k", required=True, type=int)
parser.add_argument("-mS", dest="maxSuppression", required=True, type=float)
parser.add_argument("-ct", "--columnTarget", dest="columnTarget", required=True)
parser.add_argument("-op", "--outputPath", dest="outputPath")
parser.add_argument("-oid", "--outputId", dest="outputId")

args = parser.parse_args()

rawdata = pd.read_csv(args.filename)
score = objective_function(rawdata, args.columnsSource, args.columnTarget, args.k, args.maxSuppression)

if args.outputId is not None:
    outputfile = ''
    if args.outputPath is not None:
        outputfile += args.outputPath + "/"
    outputfile += args.outputId
    with open(outputfile, "w") as myfile:
        myfile.write(str(datetime.datetime.now()) + ";"+ str(score) + "\n")

