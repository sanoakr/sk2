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
    }
)

# create Notes
df["Notes"] = df["Build"] + "_" + df["Floor"] + "_" + df["Room"]

### add Beacons
## Major 16bits = Unused(2)+SignalType(2)+BeaconFlag(1)+BeaconType(11)
## //// Major 16bits = Unused(3)+BeaconFlag(1)+BeaconType(8)+SignalType(4)
## Minor 16bits = Sequency

bFlag = 1 << 12
bType = {(1, "STB001", "固定ビーコン"), (2, "STB002", "携帯ビーコン"), (3, "STB003", "ボタンビーコン")}
bSignal = {(0b00, "S"), (0b01, "D"), (0b11, "L")}

minorNum = 4

for (i, code, type) in bType:
    for (val, opr) in bSignal:
        sFlag = val << 13
        for n in range(1, minorNum + 1):
            nStr = str(n).zfill(3)
            bData = pd.DataFrame(
                [
                    code + opr + "-" + nStr,
                    type,
                    opr,
                    nStr,
                    sFlag + bFlag + i,
                    n,
                    type + "_" + opr + "_" + nStr,
                ],
                index=df.columns,
            ).T
            df = df.append(bData, ignore_index=True)

print(df.to_json(orient="index", force_ascii=False))
