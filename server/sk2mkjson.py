# AP csv リストから Beacon を追加した csv をつくる
# sk2mkjson.py < foo.csv > foo.json

import sys
import pandas as pd
import json

# read csv
df = pd.read_csv(sys.argv[1])

# drop column
drop_col = {
    "seq",
    "クライアント数(混雑)",
    "クライアント数(やや混雑)",
    "通信量(混雑)",
    "通信量(やや混雑)",
    "UUID",
    "クライアント混雑状況進捗",
    "直近1時間のアクセス数",
    "直近1時間の通信量",
    "状態",
}
for i in drop_col:
    del df[i]

# drop lines
df = df[df["メジャー番号"].astype(int) < 2 ** 16]
df = df[df["マイナー番号"].astype(int) < 2 ** 16]

# rename
df = df.rename(
    columns={
        "ホスト名": "Name",
        "建屋": "Build",
        "フロア": "Floor",
        "場所": "Room",
        "メジャー番号": "Major",
        "マイナー番号": "Minor",
        #"UUID": "Uuid",
    }
)

# create Notes
df["Notes"] = df["Build"] + "_" + df["Floor"] + "_" + df["Room"]

### add Beacons
## Major 16bits = Unused(2)+SignalType(2)+BeaconFlag(1)+BeaconType(11)
## //// Major 16bits = Unused(3)+BeaconFlag(1)+BeaconType(8)+SignalType(4)
## Minor 16bits = Sequency

## 固定ビーコン配置
bPlace = {
    1: "瀬田バス停北",
    2: "瀬田バス停南",
    3: "瀬田生協売店",
}

bFlag = 1 << 11
bType = {(1, "STB001", "固定ビーコン"), (2, "STB002", "携帯ビーコン"), (3, "STB003", "ボタンビーコン")}
bSignal = {(0b00, "S"), (0b01, "L"), (0b11, "D")}

minorNum = 4
#uuid = "ebf59ccc-21f2-4558-9488-00f2b388e5e6"

for (i, code, type) in bType:
    for (val, opr) in bSignal:
        sFlag = val << 12
        for n in range(1, minorNum + 1):
            if i == 1 and n in bPlace:
                bName = type + "／" + bPlace[n]
            else:
                bName = type
            nStr = str(n).zfill(3)
            bData = pd.DataFrame(
                [
                    code + opr + "-" + nStr,
                    bName,
                    opr,
                    nStr,
                    #uuid,
                    sFlag + bFlag + i,
                    n,
                    bName + "_" + opr + "_" + nStr,
                ],
                index=df.columns,
            ).T
            df = df.append(bData, ignore_index=True)

print(df.to_json(orient="records", force_ascii=False))
