# sk2 JSON から個別領域用の Major/Minor CSV をつくる

import sys
import pandas as pd
import json

df = pd.read_json(sys.argv[1])
# print(df.columns.values)

# 建物セット
buildSet = set()
# フロア辞書 {建物: {フロアセット}}
floorDict = dict()

for index, row in df.iterrows():
    build = df.loc[index, "Build"]
    floor = df.loc[index, "Floor"]
    if floor == None or build == None:
        continue

    # print(build, floor)
    buildSet.add(build)

    if build in floorDict:
        floorDict[build].append(floor)
    else:
        floorDict[build] = [floor]

buildList = list(buildSet)
buildList.sort()

for b in floorDict:
    floorList = list(set(floorDict[b]))  # unique
    floorList.sort()
    floorDict[b] = floorList

# print(buildList)
# print(floorDict)

minor = 0
# range 分割数
rangeSize = 10
# floor ごとのAPカウント配列
rangeCount = [0] * 2 ** 8
# minor シフト数
rangeShift = 0

for index, row in df.iterrows():
    build = df.loc[index, "Build"]
    floor = df.loc[index, "Floor"]
    if floor == None or build == None:
        continue

    if not ("ビーコン" in row["Build"]):
        # Minor
        df.loc[index, "Minor"] = minor
        minor += 1

        # Major
        major8up = buildList.index(build)
        tmp8Low = floorDict[build].index(floor)
        rangeCount[tmp8Low] += 1
        if rangeCount[tmp8Low] % rangeSize == 0:
            rangeShift += 1

        major8low = tmp8Low + rangeShift
        major = (major8up << 8) + major8low

        df.loc[index, "Major"] = major

print(df.to_csv())
