# README #

## What's sk2? ##

龍大理工学部で計画されている学生向けの自動出欠ログシステムです。

----

## memo & Todo ##

- ログデータ登録時にもキーを送信する？
- BLEビーコン登録時にビーコンIDも記録する？
- 認証更新のタイミングは？（現在180日で再認証）
    - パスワード変更をどうdetectする? -> AD から取れる？
    - 全てのアクセスでEDSを叩くのは非現実的
    - パスワードを保存しておくのもダメ
- ログ記録と履歴読み出しが分離したので Kafka 経由のデータベース作成もあり
- デバイスID（AndroidID など）をキーに含めるか？
- sk2 サーバが兼任している認証とデータ登録を分離？

----

## sk2 サーバ ##

* 認証サーバ、出席データ登録用サーバ (attend_server.py)
* sk2.st.ryukoku.ac.jp:4440 (133.83.80.65:4440)
* 送信データに改行を含んじゃダメ、送信データは(utf-8)バイト列で
* byted utf-8 text csv

#### 認証時アクセス ####
##### 送信データ #####
* CSVデータサイズ = 3
* "AUTH, userid, password"

| データ番号 | 内容 | データ |
|:----:|:------|:------|
| 1 | 認証用定数 | "AUTH" |
| 2 | 全学認証ID | @mail.ryukoku.ac.jp なし |
| 3 | 全学認証パスワード | - |

##### 受信データ #####
* 認証成功時
    * CSVデータサイズ = 3
    * "Key, gcos, name"

| データ番号 | 内容 | データ |
|:----:|:------|:------|
| 1 | アクセスキー | hash(userid + salt) <font color="Red">[key生成にdeviceIDを含めたい]</font>|
| 2 | アルファベット氏名 | e.g. "Ryukoku Rikou" |
| 3 | 漢字氏名 | e.g. "龍谷 理工" |

* 認証失敗時
    * CSVデータサイズ = 1
    * "authfail"

| データ番号 | 内容 | データ |
|:----:|:------|:------|
| 1 | 失敗時定数 | "authfail" <font color="Red">[変更したい]</font>|

#### データ登録時アクセス ####
##### 送信データ #####
* CSVデータサイズ >= 6
* "userid, marker, datetime ,APdata0 [,APdata1[, APdata2]]...]

| データ番号 | 内容 | データ |
|:----:|:------|:------|
| 1 | 全学認証ID | @mail.ryukoku.ac.jp なし <font color="Red">[この後にkeyを追加したい]</font>|
| 2 | ログタイプ | 1char [A/M/B]：Auto/Manual/BLE |
| 3 | 日時 | yyyy-MM-dd HH-mm-ss |
| 4 | AP1 SSID | e.g "ryu-wireless" |
| 5 | AP1 BSSID | e.g. "12:34:56:78:90:ab" |
| 6 | AP1 信号強度 | e.g. "-43" |
| 7 | (Ap2 SSID) | e.g. "ryu-wireless2" |
| : | : | : |

##### 受信データ #####
* CSVデータサイズ = 1
* ログ記録成功：“success” <font color="Red">[変更したい]</font>
* 失敗： “fail” <font color="Red">[変更したい]</font>

----

## sk2 info サーバ ##
* 出席記録取得用サーバ(attend_infoServer.py)
* sk2.st.ryukoku.ac.jp:4441 (133.83.80.65:4441)
* 送信データに改行を含んじゃダメ、送信データは(utf-8)バイト列で
* byted utf-8 text csv

### 送信データ ###
* CSVデータサイズ = 2
* "userid, key"

| データ番号 | 内容 | データ |
|:----:|:------|:------|
| 1 | 全学認証ID | @mail.ryukoku.ac.jp なし|
| 2 | 認証時に受け取ったキー | hash(user + salt) <font color="Red">[key生成にdeviceIDを含めたい]</font>|

### 受信データ ###
* 成功時：過去最大5つの履歴データをCSVのまま返信：1行1レコード
* 失敗時： <font color="Red">[変更したい]</font>

| エラー内容 | 返信データ定数 |
|:-----------|:----------|
| 送信データ形式のエラー | “Illegal data format” <font color="Red">[変更したい]</font>|
| 履歴取得用キーの不一致 | “Wrong key” <font color="Red">[変更したい]</font>|
| それ以外 | “Any data I/O error occured…” <font color="Red">[変更したい]</font>|

----

## sk2 Record Table (MariaDB) ##

| Field    | Type        | Null | Key | Default | Extra |
|:---------|:------------|:----:|:---:|:-------:|:-----:|
| id       | varchar(32) | YES  |     | NULL    |       |
| type     | varchar(4)  | YES  |     | NULL    |       |
| datetime | datetime    | YES  |     | NULL    |       |
| ssid0    | varchar(32) | YES  |     | NULL    |       |
| bssid0   | varchar(32) | YES  |     | NULL    |       |
| signal0  | int(11)     | YES  |     | NULL    |       |
| ssid1    | varchar(32) | YES  |     | NULL    |       |
| bssid1   | varchar(32) | YES  |     | NULL    |       |
| signal1  | int(11)     | YES  |     | NULL    |       |
| ssid2    | varchar(32) | YES  |     | NULL    |       |
| bssid2   | varchar(32) | YES  |     | NULL    |       |
| signal2  | int(11)     | YES  |     | NULL    |       |
| ssid3    | varchar(32) | YES  |     | NULL    |       |
| bssid3   | varchar(32) | YES  |     | NULL    |       |
| signal3  | int(11)     | YES  |     | NULL    |       |
| ssid4    | varchar(32) | YES  |     | NULL    |       |
| bssid4   | varchar(32) | YES  |     | NULL    |       |
| signal4  | int(11)     | YES  |     | NULL    |       |
| :        |  :          | :    | :   | :       | :     |
	