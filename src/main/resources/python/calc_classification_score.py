from enum import Enum

import pandas as pd
from sklearn.model_selection import train_test_split
from catboost import CatBoostClassifier, CatBoostError
from sklearn.metrics import roc_auc_score, accuracy_score, f1_score, balanced_accuracy_score

from argparse import ArgumentParser
import numpy as np
import datetime

# https://pandas.pydata.org/pandas-docs/stable/user_guide/indexing.html#returning-a-view-versus-a-copy
pd.options.mode.copy_on_write = True

FLAG_CALC_ACCURACY = True
FLAG_CALC_BALANCED_ACCURACY = True
FLAG_CALC_F1_SCORE = True

class ProblemType(Enum):
    BINARY = 1
    MULTI_CLASS = 2

# Based (heavily adjusted) on https://github.com/desstaw/DataPrivacy_SimulatedAnnealing

def score_binary(model, x_test, y_test):
    # Predict on the testing data
    y_pred = model.predict(x_test)

    true_val = y_test.iloc[0]

    # Convert labels to binary values (0 and 1)
    y_test_binary = np.where(y_test == true_val, 1, 0)
    y_pred_binary = np.where(y_pred == true_val, 1, 0)

    # Calculate the AUC-ROC score
    auc_roc = roc_auc_score(y_test_binary, y_pred_binary)
    print("AUC-ROC Score (binary):", auc_roc)

    if FLAG_CALC_ACCURACY:
        accuracy = accuracy_score(y_test, y_pred)
        print("accuracy of trained model (binary):", accuracy)
    if FLAG_CALC_F1_SCORE:
        score_f1 = f1_score(y_test, y_pred, average='binary', pos_label=true_val)
        print("f1Score of trained model (binary):", score_f1)
    if FLAG_CALC_BALANCED_ACCURACY:
        score_balanced_accuracy = balanced_accuracy_score(y_test, y_pred)
        print("balanced accuracy of trained model (binary):", score_balanced_accuracy)

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

    auc_roc = roc_auc_score(y_test.to_numpy(), y_pred, labels=model.classes_, multi_class="ovo", average='macro')
    print("AUC-ROC Score (multiclass):", auc_roc)

    if FLAG_CALC_ACCURACY or FLAG_CALC_F1_SCORE or FLAG_CALC_BALANCED_ACCURACY:
        y_pred_class = model.predict(x_test,
                                     prediction_type='Class',
                                     task_type="CPU")

    if FLAG_CALC_ACCURACY:
        score_accuracy = accuracy_score(y_test, y_pred_class)
        print("accuracy of trained model (multiclass):", score_accuracy)
    if FLAG_CALC_F1_SCORE:
        score_f1 = f1_score(y_test, y_pred_class, average='micro')
        print("f1Score of trained model (multiclass):", score_f1)
    if FLAG_CALC_BALANCED_ACCURACY:
        score_balanced_accuracy = balanced_accuracy_score(y_test, y_pred_class)
        print("balanced accuracy of trained model (multiclass):", score_balanced_accuracy)

    return auc_roc

def suppress(data, relevant_columns, minimum_occurences):
    grouped = data.groupby(relevant_columns)

    suppression = {groupname: rows for (groupname, rows) in grouped.groups.items() if len(rows) < minimum_occurences}
    filtered_debug = [
        index
        for (group, indexes) in suppression.items()
        for index in indexes
    ]
    # Filter the original dataframe to remove suppressed rows
    return data[~data.index.isin(filtered_debug)]

def objective_function(df, solution, column_target, k, max_suppressed_fraction):
    # Calculate the percentage of suppressed rows -- utility
    total_rows = len(df)
    print('total rows ', total_rows)
    data_filtered = suppress(df, solution, k)

    problemtype = None

    if len(pd.unique(df[column_target])) > 2:
        problemtype = ProblemType.MULTI_CLASS
    else:
        problemtype = ProblemType.BINARY

    if problemtype == ProblemType.MULTI_CLASS:
        # for multi classification, we also have to suppress classes with less than 2 occurences, or splitting and scoring won't work
        data_filtered = suppress(data_filtered, [column_target], 2)

    suppressed_rows = total_rows - len(data_filtered)
    suppressed_fraction = suppressed_rows / total_rows
    print('SUPPRESSION FRACTION', suppressed_fraction)
    print('Max Suppressed Fraction:', max_suppressed_fraction)

    suppressed_fraction = round(suppressed_fraction, 2)
    if suppressed_fraction <= max_suppressed_fraction: #+ 1e-6:
        print('FILTERED DF INFO', data_filtered.info())

        # Calculate and return classification accuracy as the objective function score
        classification_accuracy = calculate_classification_accuracy(solution, column_target, data_filtered, problemtype)
        print('Classification accuracy (AUC-ROC)', classification_accuracy)
        return classification_accuracy

    else: #suppressed_fraction > max_suppressed_fraction:
        # If the constraint is violated, return a penalty (negative infinity)
        print('SUPPRESSION EXCEEDED LIMIT')
        return -float('inf')



def calculate_classification_accuracy(columns_source, column_target, df, problem_type):
    # Convert categorical features to strings
    categorical_features_indices = df[columns_source].select_dtypes(include=['object']).columns
    print('categorical features', categorical_features_indices)

    random_seed = 32
    np.random.seed(random_seed)
    # Split the data into training and testing sets
    # rare classes still need to exist in both training and testing, so we separate two examples for each class
    if problem_type == ProblemType.MULTI_CLASS:
        # Split the data into training and testing sets
        # rare classes still need to exist in both training and testing, so we separate two examples for each class
        train_indexes = []
        test_indexes = []
        for clazz in np.unique(df[column_target]):
            train, test = pd.Index(np.random.choice(df[(df[column_target] == clazz)].index, 2))
            train_indexes.append(train)
            test_indexes.append(test)

        # these we can just split randomly
        remaining_rows = df[~df.index.isin([*train_indexes, *test_indexes])]
        x_train, x_test, y_train, y_test = train_test_split(remaining_rows[columns_source], remaining_rows[column_target], test_size=0.2, random_state=random_seed)

        # then add the preselected rows back into the split
        x_train = pd.concat([x_train, df[df.index.isin(train_indexes)][columns_source]])
        y_train = pd.concat([y_train, df[df.index.isin(train_indexes)][column_target]])
        x_test = pd.concat([x_test, df[df.index.isin(test_indexes)][columns_source]])
        y_test = pd.concat([y_test, df[df.index.isin(test_indexes)][column_target]])



        # multi classification problem
        # Train the model on the training data.
        # TODO: GPU training  is not an option for large datasets until I figure out how to make batch training work
        # we need all classes present in both train and test, so we first get two examples of each class
        model = CatBoostClassifier(iterations=50, depth=10, learning_rate=0.1, loss_function='MultiClass', cat_features=list(categorical_features_indices), task_type='CPU')
        try:
            model.fit(x_train, y_train)
        except CatBoostError as e:
            print('CatBoostError!', e)
            print('Continuing, as these are usually caused by bad data (e.g. all values are constant)')
            return -float('inf')

        return score_multiclass(model, x_test, y_test)

    else:
        # binary classification
        x = df[columns_source]
        y = df[column_target]
        x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.2, random_state=random_seed)

        catboost_model = CatBoostClassifier(iterations=150, depth=10, learning_rate=0.1, loss_function='Logloss', cat_features=list(categorical_features_indices))
        try:
            catboost_model.fit(X=x_train,y=y_train)
        except CatBoostError as e:
            print('CatBoostError!', e)
            print('Continuing, as these are usually caused by bad data (e.g. all values are constant)')
            return -float('inf')

        return score_binary(catboost_model, x_test, y_test)


parser = ArgumentParser()
parser.add_argument("-f", "--file", dest="filename", required=True)
parser.add_argument("-cs", "--columnsSource", dest="columnsSource", nargs='+', required=True)
parser.add_argument("-k", dest="k", required=True, type=int)
parser.add_argument("-mS", dest="maxSuppression", required=True, type=float)
parser.add_argument("-ct", "--columnTarget", dest="columnTarget", required=True)
parser.add_argument("-op", "--outputPath", dest="outputPath")
parser.add_argument("-oid", "--outputId", dest="outputId")

args = parser.parse_args()

rawdata = pd.read_csv(args.filename, dtype=str)
df = rawdata[args.columnsSource + [args.columnTarget]]
df.fillna(value='', inplace=True)
score = objective_function(df, args.columnsSource, args.columnTarget, args.k, args.maxSuppression)

if args.outputId is not None:
    outputfile = ''
    if args.outputPath is not None:
        outputfile += args.outputPath + "/"
    outputfile += args.outputId
    print('writing to ', outputfile)
    with open(outputfile, "a") as myfile:
        myfile.write("{datetime};{score};{solution};{target}\n".format(datetime=str(datetime.datetime.now()),score=str(score), solution=args.columnsSource, target=args.columnTarget))

