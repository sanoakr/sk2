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
        # "UUID": "Uuid",
    }
)

# create Notes
df["Notes"] = df["Build"] + "_" + df["Floor"] + "_" + df["Room"]

### add Beacons
## Major 16bits = Building(8) + Floor(Reagion)(8) [Building < 200]
##              = BeaconType(8) + BeaconNum(8)   [BeaconType >= 200]
## // Major 16bits = Unused(2)+SignalType(2)+BeaconFlag(1)+BeaconType(11)
## //// Major 16bits = Unused(3)+BeaconFlag(1)+BeaconType(8)+SignalType(4)
## Minor 16bits = Sequency
##              = BeaconSignal

## 固定ビーコン配置
bPlace = {0: "瀬田バス停北", 1: "瀬田バス停南", 2: "瀬田生協売店", 3: "未設置"}

bType = {
    (200, "STB001", "固定ビーコン"),
    (201, "STB002", "携帯ビーコン"),
    (202, "STB003", "ボタンビーコン"),
}
bSignal = {(0b00, "S"), (0b01, "L"), (0b11, "D")}

BeaconNum = 4
# uuid = "ebf59ccc-21f2-4558-9488-00f2b388e5e6"

for (bTypeNum, bTypeCode, bTypeName) in bType:
    bMajorBase = bTypeNum << 8  # Major up8
    bCodeBase = bTypeCode
    bNotesBase = bTypeName
    for i in range(0, BeaconNum):
        bMajor = bMajorBase + i
        bCodeMajor = bCodeBase + "-" + str(i).zfill(3)
        bNotesMajor = bNotesBase + "_" + str(i).zfill(3)

        if bTypeNum == 202:  # ボタンビーコンでなら Minor を SignalType で
            minors = bSignal
        else:
            minors = {(0b00, "")}
            if bTypeNum == 200:  # 固定ビーコンなら配置を Notes に
                bNotesMajor += "／" + bPlace[i]

        for (mNum, mName) in minors:
            bCode = bCodeMajor + mName
            bNotes = bNotesMajor + "_" + mName
            bData = pd.DataFrame(
                [
                    bCode,  # Name
                    bTypeName,  # Build
                    str(i).zfill(3),  # Floor
                    str(mNum).zfill(3),  # Room
                    bMajor,  # Major
                    mNum,  # Minor
                    bNotes,  # Notes
                ],
                index=df.columns,
            ).T
            df = df.append(bData, ignore_index=True)

print(df.to_json(orient="records", force_ascii=False))
