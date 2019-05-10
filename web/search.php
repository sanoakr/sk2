<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="ja">
  <head>
    <title>sk2 自動出欠ログ検索フォーム</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" href="search.css">
      <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
      <script src="jquery.matchHeight.js" type="text/javascript"></script>
      <script type="text/javascript">
	(function() {
                /* matchHeight example */

                $(function() {
                    // apply your matchHeight on DOM ready (they will be automatically re-applied on load or resize)

                    // get test settings
                    var byRow = $('body').hasClass('items');

                    // apply matchHeight to each item container's items
                    $('.items-container').each(function() {
                        $(this).children('.item').matchHeight({
                            byRow: byRow
                        });
                    });

                    // test target
                    $('.target-items').each(function() {
                        $(this).children('.item-0, .item-2, .item-3').matchHeight({
                            target: $(this).find('.item-1')
                        });
                    });

                    // example of update callbacks (uncomment to test)
                    $.fn.matchHeight._beforeUpdate = function(event, groups) {
                        //var eventType = event ? event.type + ' event, ' : '';
                        //console.log("beforeUpdate, " + eventType + groups.length + " groups");
                    }

                    $.fn.matchHeight._afterUpdate = function(event, groups) {
                        //var eventType = event ? event.type + ' event, ' : '';
                        //console.log("afterUpdate, " + eventType + groups.length + " groups");
                    }
                });

            })();
      </script>

<?php
$server = "localhost";
$user = "sk2";
$pass = "sk2loglog";
$dbname = "sk2";
$dbchar = "UTF-8";
$dbtbl = "st";

$fid = 'id';
$ftype = 'type';
$fdt = 'datetime';
$date_from = 'from';
$date_to = 'to';
$fwd = 'wday';
$warray = array('*', '土', '日', '月', '火', '水', '木', '金');
$fhour = 'priod';
$parray = array('*', '1講時', '2講時', '昼休み', '3講時', '4講時', '5講時', '6講時');
$parray_from = array('*', '09:20', '11:05', '12:40', '13:35', '15:20', '17:00', '18:40');
$parray_to = array('*', '10:05', '12:35', '13:30', '15:05', '16:50', '18:30', '20:10');
$parray_dist = array(0, 5, 10, 15, 20, 30, 40, 50);
$fmajor = 'major';
$fminor = 'minor';
$fdist = 'distance';
$fplace = 'place';
$apnum = 3;

$json_file = 'Seta_wifi_001.json';
?>
    </head>
    <body>
      <div class="container">
        <h1>sk2 自動出欠ログ検索フォーム</h1>
<?php
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
    echo "<h2 class='test-summary'>sk2 has " . $row[0] . " records.</h2>";
}
//////////////////////////////
if ($_SERVER["REQUEST_METHOD"] == 'POST') {
    $$fid = $_POST[$fid];
    $$ftype = $_POST[$ftype];

    //foreach ($farr as $key) {
    //    $$key = $_POST[$key];
    //    //echo " $key=" . $$key;
    //}
    //unset($key);

    $$date_from = $_POST[$date_from];
    $$date_to = $_POST[$date_to];
    $$fwd = $_POST[$fwd];
    $$fhour = $_POST[$fhour];
    //foreach ($aparr as $key) {
    //    $$key = $_POST[$key];
    //    //echo " $key=" . $$key;
    //}
    //unset($key);
    $$fmajor = $_POST[$fmajor];
    $$fminor = $_POST[$fminor];
    $$fdist = $_POST[$fdist];
    $$fplace = $_POST[$fplace];
} else {
    $$fid = '%';
    $$ftype = '*';
    $$date_from = '2019-05-01';
    $$date_to = date("Y-m-d");
    $$fwd = '*';
    $$fhour = '*';;
    $$fmajor = '*';
    $$fminor = '*';
    $$fdist = '0';
    $$fplace = '*';
}

//////////////////////////////
$json = file_get_contents($json_file);
$json_arr = json_decode($json, true);
foreach ($json_arr as $ap) {
  if ($ap["Notes"] != null) {
    $s_place = explode('_', $ap["Notes"]);
    $build[$ap["Major"]][$ap["Minor"]] = $s_place[0];
    $floor[$ap["Major"]][$ap["Minor"]] = $s_place[1];
    $room[$ap["Major"]][$ap["Minor"]] = $s_place[2];
    //echo $room[$ap["Major"]][$ap["Minor"]] . '<br>';
    $place_major["$s_place[0]_$s_place[1]_$s_place[2]"] = $ap["Major"];
    $place_minor["$s_place[0]_$s_place[1]_$s_place[2]"] = $ap["Minor"];
    ksort($place_major);
    ksort($place_minor);
  }
}

//////////////////////////////
echo <<< EOF
<form action='search.php' method ='post'>
<div class="items-container items">
EOF;
echo '<div class="item item-0"><h3>学籍番号</h3>';
echo '<p><input type="text" name="' . $fid . '" value=' . $$fid . '>&emsp;(wildcard=%)<p>';
echo <<< EOF
<p>
% 「全ユーザー」<br>
T% 「理工学部生」<br>
T19% 「理工学部の2019年度入学生」<br>
</p></div>
EOF;

echo '<div class="item item-1"><h3>送信タイプ</h3><p>';
makeSelector($link, $dbtbl, $ftype, $$ftype);
echo <<< EOF
</p>
<p>M 「手動送信」<br>
A 「自動送信」<br>
</p></div>
EOF;

echo '<div class="item item-2"><h3>日付</h3><p>';
makeDtSelector($link, $dbtbl, $fdt, $date_from, $date_to, $$date_from, $$date_to);
echo <<< EOF
</p><p>
</p></div>
EOF;

echo '<div class="item item-3"><h3>曜日</h3><p>';
makeWdSelector($link, $dbtbl, $fwd, $warray, $$fwd);
echo <<< EOF
</p><p>
</p></div>
EOF;

echo '<div class="item item-4"><h3>講時</h3><p>';
makeHpSelector($link, $dbtbl, $fhour, $parray, $parray_from, $parray_to, $$fhour);
echo <<< EOF
</p><p>
</p></div>
EOF;

echo '<div class="item item-5"><h3>教室</h3><p>';
makeRoomSelector($link, $dbtbl, $fplace, $place_major, $$fplace);
echo <<< EOF
</p><p>
</p></div>
EOF;

echo '<div class="item item-6"><h3>Major</h3><p>';
makeApSelector($link, $dbtbl, $fmajor, $apnum, $$fmajor);
echo <<< EOF
</p>
<p>M 「手動送信」<br>
A 「自動送信」<br>
</p></div>
EOF;

echo '<div class="item item-7"><h3>Minor</h3><p>';
makeApSelector($link, $dbtbl, $fminor, $apnum, $$fminor);
echo <<< EOF
</p>
<p>M 「手動送信」<br>
A 「自動送信」<br>
</p></div>
EOF;

echo '<div class="item item-8"><h3>距離</h3><p>';
makeApSelector($link, $dbtbl, $fdist, $apnum, $$fdist);
echo <<< EOF
</p>
<p>M 「手動送信」<br>
A 「自動送信」<br>
</p></div>
EOF;

echo <<< EOF
</div>
<p>
<button type='submit' value='search' class='button'>検索</button>
</p>
</form>
EOF;

//////////////////////////////
if ($_SERVER["REQUEST_METHOD"] == 'POST') {
    echo "<h3></h3>\n";
    $sql = "SELECT * FROM $dbtbl WHERE ";
    //////////////////////////////
    $sql .= "$fid LIKE '" . $$fid . "' AND ";
    if ($$ftype != '*') {
        $sql .= "$ftype='" . $$ftype . "' AND ";
    }
    //////////////////////////////
    //foreach ($farr as $key) {
    //    if ($$key != '*') {
    //        $sql .= "$key='" . $$key . "' AND ";
    //    }
    //}
    //////////////////////////////
    $sql .= "( $fdt BETWEEN '" . $$date_from . " 00:00:00' AND '" . $$date_to . " 23:59:59') AND ";
    //////////////////////////////
    //foreach (array($aparr as $key) {
    //    if ($$key != '*') {
    //        $sql .= '(';
    //        for ($w = 0; $w < $apnum; $w++) {
    //            $sql .= "  $key$w='" . $$key . "' OR ";
    //        }
    //        $sql .= 'False) AND ';
    //    }
    //}
    //////////////////////////////
    if ($$fwd != 0) {
        $sql .= "dayofweek($fdt)=" . $$fwd . " AND ";
    }
    //////////////////////////////
    if ($$fhour != 0) {
        $sql .= "(time(datetime) between '" . $parray_from[$$fhour] . ":00' AND '" . $parray_to[$$fhour] . ":00') AND ";
        //$sql .= "( date_format(datetime, '%h:%m:%s') between '" . $parray_from[$$fhour] . ":00' AND '" . $parray_to[$$fhour] . ":00') AND ";
    }
    //////////////////////////////
    if ($$fplace != '*') {
        $sql .= "(";
	$sql .= "(" . $fmajor . "0='" . $place_major[$$fplace] . "' AND " . $fminor . "0='" . $place_minor[$$fplace] . "') OR ";
	$sql .= "(" . $fmajor . "1='" . $place_major[$$fplace] . "' AND " . $fminor . "1='" . $place_minor[$$fplace] . "') OR ";
	$sql .= "(" . $fmajor . "2='" . $place_major[$$fplace] . "' AND " . $fminor . "2='" . $place_minor[$$fplace] . "')";
        $sql .= ") AND ";
    }
    //////////////////////////////
    $sql .= "True Limit 9999";
    echo "<p>${sql}</p>\n";

    echo "<h2>Search Result: ";
    if ($result = $link->query($sql)) {
        echo "$result->num_rows records found.</h2>";
        echo "<div><pre>\n";
	$data = array();
        while ($row = $result->fetch_assoc()) {
            foreach ($row as $key => $value) {
                $data[$key] = $value;
                echo "$value, ";
            }
	    for ($i=0; $i<$apnum; $i++) {
              if (!empty($room[$data["major$i"]][$data["minor$i"]])) {
  	        echo $room[$data["major$i"]][$data["minor$i"]];
              }
              echo ", ";
            }
            echo "<br>";
        }
        echo "</pre></div>\n";
    } else {
        echo "</h2>\n<p>Query Error</p><br>\n";
    }
}
$link->close();
?>
   </div></p>
	</div>
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
function makeRoomSelector($link, $dbtbl, $key, $place_major, $init = '*')
{
    echo '<select name="' . $key . '">' . "\n";
    echo '<option value="*"';
    if ($init == '*') {
        echo ' selected';
    }
    echo '>*</option>' . "\n";
    foreach ($place_major as $place => $val) {
        echo '<option value=' . $place;
        if ($init == $place) {
            echo ' selected';
        }
        echo '>' . $place . "</option>\n";
    }
    echo "</select>\n";
}
?>

