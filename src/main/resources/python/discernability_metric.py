from argparse import ArgumentParser
import re
import pandas as pd
import numpy as np

parser = ArgumentParser()
parser.add_argument("-f", "--file", dest="filename", required=True)
parser.add_argument("-cs", "--columnsSource", dest="columnsSource", nargs='+', required=True)
parser.add_argument("-k", dest="k", required=True, type=int)

args = parser.parse_args()

rawdata = pd.read_csv(args.filename, dtype=str)
df = rawdata[args.columnsSource]
df.fillna(value='', inplace=True)

total_rows = df.shape[0]
grouped = df.groupby(args.columnsSource)

groups_at_least_k = 0
running_sum_at_least_k = 0
groups_below_k = 0
running_sum_below_k = 0

for (name, group) in grouped:
    size = group.shape[0]
    if size >= args.k:
        groups_at_least_k += 1
        running_sum_at_least_k += size * size
    else:
        groups_below_k += 1
        running_sum_below_k += size

print("groupcount below: '" + str(groups_below_k) + "'; groups above: '" + str(groups_at_least_k) + "'")
print("running_sum_below_k=" + str(running_sum_below_k) + "; running_sum_at_least_k=" + str(running_sum_at_least_k))
print("discernibility: ", (running_sum_below_k * total_rows) + running_sum_at_least_k)