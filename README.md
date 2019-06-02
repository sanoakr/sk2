# README #

## What's sk2? ##

龍大理工学部で計画されている学生向けの自動出欠ログシステムです。

----
## memo & Todo ##
- 認証更新のタイミングは？（現在180日で再認証）
    - パスワード変更をどうdetectする? -> AD から取れる？
    - 全てのアクセスでEDSを叩くのは非現実的
    - パスワードを保存しておくのもダメ
- sk2 サーバが兼任している認証とデータ登録を分離？

----

## sk2 サーバ ##

* 認証サーバ、出席データ登録用サーバ (sk2.py)
* sk2.st.ryukoku.ac.jp:4440 (133.83.80.65:4440)
* 送信データに改行を含んじゃダメ、送信データは(utf-8)バイト列で
* byted utf-8 text csv

### 認証時アクセス ###
#### 送信データ ####
* CSVデータサイズ = 4
* "AUTH, userid, password"

| データ番号 | 内容 | データ |
|:----:|:------|:------|
| 1 | 認証用定数 | "AUTH" |
| 2 | 全学認証ID | @mail.ryukoku.ac.jp なし |
| 3 | 全学認証パスワード | - |

#### 受信データ ####
* 認証成功時
    * CSVデータサイズ = 3
    * "Key, gcos, name"

| データ番号 | 内容 | データ |
|:----:|:------|:------|
| 1 | アクセスキー | hash(userid + salt) |
| 2 | アルファベット氏名 | e.g. "Ryukoku Rikou" |
| 3 | 漢字氏名 | e.g. "龍谷 理工" |
| 4 | AP情報JSON | e.g. String([{"lanIp":"192.168.11.62","serial":"Q2FW-65X2-A5KL",...) |

* 認証失敗時
    * CSVデータサイズ = 1
    * "authfail"

| データ番号 | 内容 | データ |
|:----:|:------|:------|
| 1 | 失敗時定数 | "authfail" </font>|

----

### データ登録時アクセス ###
#### 送信データ ####
* CSVデータサイズ = 7
* "userid, key, marker, datetime ,APdata0 [,APdata1[, APdata2]]...]

| データ番号 | 内容 | データ |
|:----:|:------|:------|
| 1 | 全学認証ID | @mail.ryukoku.ac.jp なし |
| 2 | 認証時に受け取ったキー | hash(user + salt) |
| 3 | ログタイプ | 8char [A/M]：Auto/Manual + Version Info|
| 4 | 日時 | yyy-MM-dd HH-mm-ss |
| 5 | Beacon0 Major | 整数値文字列 |
| 6 | Beacon0 Minor | 整数値文字列 |
| 7 | Beacon0 距離 | 浮動小数点数文字列 e.g. "0.12345" |
| 8 | Beacon1 Major | 整数値文字列 |
| : | : | : |

#### 受信データ ####
* CSVデータサイズ = 1
* ログ記録成功：“success
* 認証失敗： "authfail"
* 失敗： "fail"

----

## sk2 info サーバ ##
* 出席記録取得用サーバ(sk2info.py)
* sk2.st.ryukoku.ac.jp:4441 (133.83.80.65:4441)
* 送信データに改行を含んじゃダメ、送信データは(utf-8)バイト列で
* byted utf-8 text csv

### 送信データ ###
* CSVデータサイズ = 2
* "userid, key"

| データ番号 | 内容 | データ |
|:----:|:------|:------|
| 1 | 全学認証ID | @mail.ryukoku.ac.jp なし|
| 2 | 認証時に受け取ったキー | hash(user + salt) |

### 受信データ ###
* 成功時：過去最大5つの履歴データをCSVのまま返信：1行1レコード
* 失敗時： <font color="Red">[変更したい]</font>

| エラー内容 | 返信データ定数 |
|:-----------|:----------|
| 送信データ形式のエラー | “Illegal data format |
| 履歴取得用キーの不一致 | “Wrong key |
| それ以外 | “Any data I/O error occured…|

----

## sk2 Record Table (MariaDB) ##

```
MariaDB [sk2]> desc base;
+-----------+----------------------+------+-----+---------+-------+
| Field     | Type                 | Null | Key | Default | Extra |
+-----------+----------------------+------+-----+---------+-------+
| id        | varchar(128)         | YES  |     | NULL    |       |
| type      | varchar(8)           | YES  |     | NULL    |       |
| datetime  | datetime             | YES  |     | NULL    |       |
| major0    | smallint(5) unsigned | YES  |     | NULL    |       |
| minor0    | smallint(5) unsigned | YES  |     | NULL    |       |
| distance0 | float unsigned       | YES  |     | NULL    |       |
| major1    | smallint(5) unsigned | YES  |     | NULL    |       |
| minor1    | smallint(5) unsigned | YES  |     | NULL    |       |
| distance1 | float unsigned       | YES  |     | NULL    |       |
| major2    | smallint(5) unsigned | YES  |     | NULL    |       |
| minor2    | smallint(5) unsigned | YES  |     | NULL    |       |
| distance2 | float unsigned       | YES  |     | NULL    |       |
+-----------+----------------------+------+-----+---------+-------+
12 rows in set (0.00 sec)
```
