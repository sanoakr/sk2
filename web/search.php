<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="ja">
    <head>
        <title>自動出欠ログ検索フォーム</title>
          <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
            <style type="text/css">
              /*<![CDATA[*/
            body {
                background-color: #fff;
                color: #000;
                font-size: 1em;
                font-family: sans-serif,helvetica;
                margin: 0;
                padding: 0;
            }
            :link {
                color: #c00;
            }
            :visited {
                color: #c00;
            }
            a:hover {
                color: #f50;
            }
            h1 {
                text-align: center;
                margin: 0;
                padding: 1em 2em 1em;
                background-color: #294172;
                color: #fff;
                font-weight: normal;
                font-size: 1.5em;
                border-bottom: 1px solid #000;
            }
            h1 strong {
                font-weight: bold;
                font-size: 1.3em;
            }
            h2 {
                text-align: center;
                background-color: #3C6EB4;
                font-size: 1.2em;
                font-weight: bold;
                color: #fff;
                margin: 0;
                padding: 0.2em;
                border-bottom: 1px solid #294172;
            }
            hr {
                display: none;
            }
            .form {
                font-size: 1em;
                padding: 2em;
            }
            .content {
                padding: 1em;
            }
            .alert {
                border: 1px solid #000;
            }
            select {
                font-size: 1em;
                border: 1px;
            }
            img {
                border: 2px solid #fff;
                padding: 2px;
                margin: 2px;
            }
            a:hover img {
                border: 1px solid #294172;
            }
            .logos {
                margin: 1em;
                text-align: center;
            }
            /*]]>*/
        </style>
    </head>

    <body>
        <h1>自動出欠ログ検索フォーム</h1>
        <div class="form"><p>
        <?php
$server = "localhost";
$user = "sk2";
$pass = "sk2loglog";
$dbname = "sk2";
$dbchar = "UTF-8";
$dbtbl = "test";

$farr = array('id', 'type');
$fdt = 'datetime';
$date_from = 'from';
$date_to = 'to';
$fwd = 'wday';
$warray = array('*', '日', '月', '火', '水', '木', '金', '土');
$fhour = 'priod';
$parray = array('*', '1講時', '2講時', '3講時', '4講時', '5講時', '6講時', '昼休み');
$parray_from = array('*', '09:20', '11:05', '13:35', '15:20', '17:00', '18:40', '12:40');
$parray_to = array('*', '10:05', '12:35', '15:05', '16:50', '18:30', '20:10', '13:30');
$aparr = array('ssid', 'bssid', 'signal');
$apnum = 5;

//////////////////////////////
$link = new mysqli($server, $user, $pass, $dbname);
if ($sql_error = $link->connect_error) {
    error_log($sql_error);
    die($sql_error);
} else {
    $link->set_charset($dbchar);
    //echo "connect and use success!<br>\n";
}
$sql = "SELECT COUNT(*) FROM $dbtbl";
if ($result = $link->query($sql)) {
    $row = $result->fetch_row();
    echo "<p>sk2 has " . $row[0] . " records.</p>\n";
}
echo "<h2></h2><br>\n";
//////////////////////////////
if ($_SERVER["REQUEST_METHOD"] == 'POST') {
    foreach ($farr as $key) {
        $$key = $_POST[$key];
        //echo " $key=" . $$key;
    }
    unset($key);
    $$date_from = $_POST[$date_from];
    $$date_to = $_POST[$date_to];
    $$fwd = $_POST[$fwd];
    $$fhour = $_POST[$fhour];
    foreach ($aparr as $key) {
        $$key = $_POST[$key];
        //echo " $key=" . $$key;
    }
    unset($key);
}
//////////////////////////////
echo "<div name='form'><form action='search.php' method ='post'>\n";
foreach ($farr as $key) {
    echo "&emsp;&emsp;$key:";
    makeSelector($link, $dbtbl, $key, $$key);
}
unset($key);

echo "&emsp;&emsp;date:";
makeDtSelector($link, $dbtbl, $fdt, $date_from, $date_to, $$date_from, $$date_to);
echo "&emsp;&emsp;weekday:";
makeWdSelector($link, $dbtbl, $fwd, $warray, $$fwd);
echo "&emsp;&emsp;period:";
makeHpSelector($link, $dbtbl, $fhour, $parray, $parray_from, $parray_to, $$fhour);

foreach ($aparr as $key) {
    echo "&emsp;&emsp;$key:";
    makeApSelector($link, $dbtbl, $key, $apnum, $$key);
}
unset($key);
echo "<br><p><input type='submit' value='search'></p></form></div>\n";

//////////////////////////////
if ($_SERVER["REQUEST_METHOD"] == 'POST') {
    echo "<h2>Search Result</h2>\n";
    $sql = "SELECT * FROM $dbtbl WHERE ";
    //////////////////////////////
    foreach ($farr as $key) {
        if ($$key != '*') {
            $sql .= "$key='" . $$key . "' AND ";
        }
    }
    //////////////////////////////
    $sql .= "( $fdt BETWEEN '" . $$date_from . " 00:00:00' AND '" . $$date_to . " 23:59:59') AND ";
    //////////////////////////////
    foreach ($aparr as $key) {
        if ($$key != '*') {
            $sql .= '(';
            for ($w = 0; $w < $apnum; $w++) {
                $sql .= "  $key$w='" . $$key . "' OR ";
            }
            $sql .= 'False) AND ';
        }
    }
    //////////////////////////////
    if ($$fwd != 0) {
        $sql .= "dayofweek($fdt)=" . $$fwd . " AND ";
    }
    //////////////////////////////
    if ($$fhour != 0) {
        $sql .= "( date_format(datetime, '%h:%m:%s') between '" . $parray_from[$$fhour] . ":00' AND '" . $parray_to[$$fhour] . ":00') AND ";
    }
    //////////////////////////////
    $sql .= "True Limit 9999";
    echo "$sql<h2></h2>\n";

    if ($result = $link->query($sql)) {
        echo "<h3>$result->num_rows records found.</h3>";
        while ($row = $result->fetch_assoc()) {
            echo "<div><pre>\n";
            foreach ($row as $key => $value) {
                echo "$value, ";
            }
            echo "\n";
        }
        echo "</pre></div>\n";
    } else {
        echo "<p>Query Error</p><br>\n";
    }
}
$link->close();
?>
   </div></p>
  </body>
</html>
<!--//////////////////////////////////////////////////-->
<?php
function makeSelector($link, $tbl, $name, $init = '*')
{
    $sql = "SELECT DISTINCT $name FROM $tbl";
    echo '<select name="' . $name . '">' . "\n";
    echo '<option value="*">*</option>' . "\n";
    if ($result = $link->query($sql)) {
        while ($row = $result->fetch_array()) {
            echo '<option value=' . $row[$name];
            if ($row[$name] == $init) {
                echo ' selected>';
            } else {
                echo '>';
            }

            echo $row[$name] . "</option>\n";
        }
    }
    echo "</select>\n";
}
function makeApSelector($link, $tbl, $key, $num, $init = '*')
{
    $sw = $key . '0';
    $sql = "SELECT DISTINCT $sw FROM $tbl ";
    for ($w = 1; $w < $num; $w++) {
        $sql .= "UNION SELECT DISTINCT $key$w FROM $tbl ";
    }
    echo '<select name="' . $key . '">' . "\n";
    echo '<option value="*">*</option>' . "\n";
    if ($result = $link->query($sql)) {
        while ($row = $result->fetch_array()) {
            echo '<option value=' . $row[$sw];
            if ($row[$sw] == $init) {
                echo ' selected>';
            } else {
                echo '>';
            }
            echo $row[$sw] . "</option>\n";
        }
    }
    echo "</select>\n";
}
function makeDtSelector($link, $tbl, $key, $key_from, $key_to, $from = '', $to = '')
{
    $date = 'date';

    $sql = "SELECT min(date_format($key, '%Y-%m-%d')) AS $date FROM $tbl";
    if ($result = $link->query($sql)) {
        $min = $result->fetch_array()[0];
    }
    if ($from == '') {$from = $min;}

    $sql = "SELECT max(date_format($key, '%Y-%m-%d')) AS $date FROM $tbl";
    if ($result = $link->query($sql)) {
        $max = $result->fetch_array()[$date];
    }
    if ($to == '') {$to = $max;}

    echo '<select name="' . $key_from . '">' . "\n";
    $sql = "SELECT DISTINCT date_format($key, '%Y-%m-%d') AS $date FROM $tbl";
    if ($result = $link->query($sql)) {
        while ($row = $result->fetch_array()) {
            echo '<option value=' . $row[$date];
            if ($row[$date] == $from) {
                echo ' selected>';
            } else {
                echo '>';
            }
            echo $row[$date] . "</option>\n";
        }
    }
    echo "</select>\n";

    echo ' 〜 <select name="' . $key_to . '">' . "\n";
    $sql = "SELECT DISTINCT date_format($key, '%Y-%m-%d') AS $date FROM $tbl";
    if ($result = $link->query($sql)) {
        while ($row = $result->fetch_array()) {
            echo '<option value=' . $row[$date];
            if ($row[$date] == $to) {
                echo ' selected>';
            } else {
                echo '>';
            }
            echo $row[$date] . "</option>\n";
        }
    }
    echo "</select>\n";
}
function makeWdSelector($link, $tbl, $key, $warray, $init = 0)
{
    $sql = "SELECT DISTINCT dayofweek(datetime) AS $key FROM $tbl";
    echo '<select name="' . $key . '">' . "\n";
    echo '<option value=0>*</option>' . "\n";
    if ($result = $link->query($sql)) {
        while ($row = $result->fetch_array()) {
            echo '<option value=' . $row[$key];
            if ($row[$key] == $init) {
                echo ' selected>';
            } else {
                echo '>';
            }
            echo $warray[$row[$key]] . "</option>\n";
        }
    }
    echo "</select>\n";
}
function makeHpSelector($link, $tbl, $key, $pary, $fromary, $toary, $init = 0)
{
    echo '<select name="' . $key . '">' . "\n";
    echo "<option value=0>*</option>\n";
    for ($i = 1; $i <= 7; $i++) {
        echo '<option value=' . $i;
        if ($i == $init) {
            echo ' selected>';
        } else {
            echo '>';
        }
        echo "$pary[$i] ($fromary[$i]〜$toary[$i])</option>\n";
    }
    echo "</select>\n";
}

?>