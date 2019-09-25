<?php
$lifetime=3*60; // 3min
session_start();
setcookie(session_name(),session_id(),time()+$lifetime);
?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="ja">

<head>
    <title>sk2 自動出欠ログ検索フォーム</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" href="search.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/themes/smoothness/jquery-ui.css">
    <script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js"></script>
    <script src="//ajax.googleapis.com/ajax/libs/jqueryui/1/i18n/jquery.ui.datepicker-ja.min.js"></script>
    <script type="text/javascript">
    function choice_select(level, arr, orgs) {
        var parent = arr[level - 1];
        var target = arr[level];
        var gchild = null;
        var pval = parent.val();

        target.html(orgs[level]).find('option').each(function() {
            var cdata = $(this).data('val');
            if (pval != cdata && '*' != cdata) {
                $(this).remove();
            }
        });

        if (pval.match(/\*/)) {
            for (var i = level; i < arr.length; i++) {
                arr[i].attr('disabled', 'disabled');
            }
        } else {
            target.removeAttr('disabled');
        }
    }
    $(function() {
        ///// 教室の階層セレクトボックス
        // リロード時に設定
        var orgs0 = [$('#build0').html(), $('#floor0').html(), $('#room0').html()];
        var orgs1 = [$('#build1').html(), $('#floor1').html(), $('#room1').html()];
        var orgs2 = [$('#build2').html(), $('#floor2').html(), $('#room2').html()];

        choice_select(1, [$('#build0'), $('#floor0'), $('#room0')], orgs0);
        choice_select(2, [$('build0'), $('#floor0'), $('#room0')], orgs0);
        choice_select(1, [$('#build1'), $('#floor1'), $('#room1')], orgs1);
        choice_select(2, [$('#build1'), $('#floor1'), $('#room1')], orgs1);
        choice_select(1, [$('#build2'), $('#floor2'), $('#room2')], orgs2);
        choice_select(2, [$('#build2'), $('#floor2'), $('#room2')], orgs2);

        // 変更時に設定
        $('#build0').change(function() {
            choice_select(1, [$('#build0'), $('#floor0'), $('#room0')], orgs0);
            choice_select(2, [$('#build0'), $('#floor0'), $('#room0')], orgs0);
        });
        $('#floor0').change(function() {
            choice_select(2, [$('#build0'), $('#floor0'), $('#room0')], orgs0);
        });
        $('#build1').change(function() {
            choice_select(1, [$('#build1'), $('#floor1'), $('#room1')], orgs1);
            choice_select(2, [$('#build1'), $('#floor1'), $('#room1')], orgs1);
        });
        $('#floor1').change(function() {
            choice_select(2, [$('#build1'), $('#floor1'), $('#room1')], orgs1);
        });
        $('#build2').change(function() {
            choice_select(1, [$('#build2'), $('#floor2'), $('#room2')], orgs2);
            choice_select(2, [$('#build2'), $('#floor2'), $('#room2')], orgs2);
        });
        $('#floor2').change(function() {
            choice_select(2, [$('#build2'), $('#floor2'), $('#room2')], orgs2);
        });
        ///// 教室のチェックボックス
        // リロード時に設定
        if ($('#roomCheck1').is(':checked')) {
            $('#build1').prop('disabled', false);
            $('#floor1').prop('disabled', false);
            $('#room1').prop('disabled', false);
        } else {
            $('#build1').prop('disabled', true);
            $('#floor1').prop('disabled', true);
            $('#room1').prop('disabled', true);
        }
        if ($('#roomCheck2').is(':checked')) {
            $('#build2').prop('disabled', false);
            $('#floor2').prop('disabled', false);
            $('#room2').prop('disabled', false);
        } else {
            $('#build2').prop('disabled', true);
            $('#floor2').prop('disabled', true);
            $('#room2').prop('disabled', true);
        }
        // 変更時に設定
        $('#roomCheck1').change(function() {
            if ($(this).is(':checked')) {
                $('#build1').prop('disabled', false);
                $('#floor1').prop('disabled', false);
                $('#room1').prop('disabled', false);
            } else {
                $('#build1').prop('disabled', true);
                $('#floor1').prop('disabled', true);
                $('#room1').prop('disabled', true);
            }
        });
        $('#roomCheck2').change(function() {
            if ($(this).is(':checked')) {
                $('#build2').prop('disabled', false);
                $('#floor2').prop('disabled', false);
                $('#room2').prop('disabled', false);
            } else {
                $('#build2').prop('disabled', true);
                $('#floor2').prop('disabled', true);
                $('#room2').prop('disabled', true);
            }
        });
        // Date Picker
        $('#datefrom').datepicker({
            dateFormat: 'yy-mm-dd',
        });
        $('#dateto').datepicker({
            dateFormat: 'yy-mm-dd',
        });
        // 曜日の全選択
        var checkAll = '#wdCheckAll';
        var checkBox = 'input[name="wday[]"]';

        $(checkAll).on('click', function() {
            $(checkBox).prop('checked', $(this).is(':checked'));
        });

        $(checkBox).on('click', function() {
            var boxCount = $(checkBox).length;
            var checked = $(checkBox + ':checked').length;
            if (checked === boxCount) {
                $(checkAll).prop('checked', true);
            } else {
                $(checkAll).prop('checked', false);
            }
        });
        // 講時の全選択
        var checkAllH = '#hpCheckAll';
        var checkBoxH = 'input[name="priod[]"]';

        $(checkAllH).on('click', function() {
            $(checkBoxH).prop('checked', $(this).is(':checked'));
        });

        $(checkBoxH).on('click', function() {
            var boxCount = $(checkBoxH).length;
            var checked = $(checkBoxH + ':checked').length;
            if (checked === boxCount) {
                $(checkAllH).prop('checked', true);
            } else {
                $(checkAllH).prop('checked', false);
            }
        });
    });
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
$warray = array('*', '日', '月', '火', '水', '木', '金', '土'); // MySQL dayofweek()
$fhour = 'priod';
$parray = array('昼休み', '1講時', '2講時', '3講時', '4講時', '5講時', '6講時');
$parray_from = array('12:40', '09:20', '11:05', '13:35', '15:20', '17:00', '18:40');
$parray_to = array('13:30', '10:50', '12:35', '15:05', '16:50', '18:30', '20:10');
$parray_dist = array(0, 5, 10, 15, 20, 30, 40, 50);
$fmajor = 'major';
$fminor = 'minor';
$fdist = 'distance';
$fbuild0 = 'build0';
$fbuild1 = 'build1';
$fbuild2 = 'build2';
$ffloor0 = 'floor0';
$ffloor1 = 'floor1';
$ffloor2 = 'floor2';
$froom0 = 'room0';
$froom1 = 'room1';
$froom2 = 'room2';
$froomck1 = 'rcheck1';
$froomck2 = 'rcheck2';
$apnum = 3;

$json_file = 'Seta_wifi_001.json';
$download = 'csvdl.php';
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
    echo '</div><div class="container">';
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
    $$fbuild0 = $_POST[$fbuild0];
    $$ffloor0 = $_POST[$ffloor0];
    $$froom0 = $_POST[$froom0];
    $$fbuild1 = $_POST[$fbuild1];
    $$ffloor1 = $_POST[$ffloor1];
    $$froom1 = $_POST[$froom1];
    $$fbuild2 = $_POST[$fbuild2];
    $$ffloor2 = $_POST[$ffloor2];
    $$froom2 = $_POST[$froom2];
    $$froomck1 = $_POST[$froomck1];
    $$froomck2 = $_POST[$froomck2];
} else {
    $$fid = '*';
    $$ftype = '*';
    $$date_from = '2019-05-01';
    $$date_to = date("Y-m-d");
    $$fwd = ['1', '2', '3', '4', '5', '6', '7'];
    $$fhour = ['0', '1', '2', '3', '4', '5', '6'];
    $$fmajor = '*';
    $$fminor = '*';
    $$fdist = '0';
    $$fbuild0 = '*';
    $$ffloor0 = '*';
    $$froom0 = '*';
    $$fbuild1 = '*';
    $$ffloor1 = '*';
    $$froom1 = '*';
    $$fbuild2 = '*';
    $$ffloor2 = '*';
    $$froom2 = '*';
    $$froomck1 = '';
    $$froomck2 = '';
}

//////////////////////////////
$json = file_get_contents($json_file);
$json_arr = json_decode($json, true);
$build_arr = array();
$floor_arr = array();
$room_arr = array();
foreach ($json_arr as $ap) {
    if ($ap["Notes"] != null) {
        $s_place = explode('_', $ap["Notes"]);
        $build[$ap["Major"]][$ap["Minor"]] = $s_place[0];
        $floor[$ap["Major"]][$ap["Minor"]] = $s_place[1];
        $room[$ap["Major"]][$ap["Minor"]] = $s_place[2];

        $place_code["$s_place[0]_$s_place[1]_$s_place[2]"][] = array($ap["Major"], $ap["Minor"]);
        $place_code["$s_place[0]_$s_place[1]"][] = array($ap["Major"], $ap["Minor"]);
        $place_code["$s_place[0]"][] = array($ap["Major"], $ap["Minor"]);

        $build_arr[] = $s_place[0];
        $floor_arr["$s_place[0]"][] = $s_place[1];
        $room_arr["$s_place[0]_$s_place[1]"][] = $s_place[2];
    }
}
$build_arr = array_values(array_unique($build_arr));
//foreach ($build_arr as $key => $val) {
//    echo "$key => $val<br>\n";
//}

foreach ($floor_arr as $key => &$val) {
    $val = array_values(array_unique($val));
}
//foreach ($floor_arr as $key => $val) {
//    foreach ($val as $k => $v) {
//        echo "$key => $v<br>\n";
//    }
//}

foreach ($room_arr as $key => &$val) {
    $val = array_values(array_unique($val));
}
//foreach ($room_arr as $key => $val) {
//    foreach ($val as $k => $v) {
//        echo "$key => $v<br>\n";
//    }
//}

//////////////////////////////
echo "<form action='search.php' method ='post'>";
echo '<div class="item"><h3>学籍番号</h3>';
echo '<p><input type="text" name="' . $fid . '" value=' . $$fid . '></p>';
echo <<< EOF
<p>検索パターンのワイルドカードは *、任意の1文字は _ です。<br>
例：「*」 = 「全ユーザー」、「T*」 = 「理工学部生」、「_19*」 = 「全ての学部の2019年度入学生」</p>
</div>
EOF;

echo '<div class="item"><h3>教室</h3><p>';
makeRoomSelector([$fbuild0, $ffloor0, $froom0], [$build_arr, $floor_arr, $room_arr], 0, $$fbuild0);
makeRoomSelector([$fbuild0, $ffloor0, $froom0], [$build_arr, $floor_arr, $room_arr], 1, $$ffloor0);
makeRoomSelector([$fbuild0, $ffloor0, $froom0], [$build_arr, $floor_arr, $room_arr], 2, $$froom0);
echo '</p><p><input type="checkbox" id="roomCheck1" name="' . $froomck1 . '" value="1"';
if ($$froomck1) {
    echo " checked='checked'";
}
echo '/>OR ';
makeRoomSelector([$fbuild1, $ffloor1, $froom1], [$build_arr, $floor_arr, $room_arr], 0, $$fbuild1);
makeRoomSelector([$fbuild1, $ffloor1, $froom1], [$build_arr, $floor_arr, $room_arr], 1, $$ffloor1);
makeRoomSelector([$fbuild1, $ffloor1, $froom1], [$build_arr, $floor_arr, $room_arr], 2, $$froom1);
echo '</p><p><input type="checkbox" id="roomCheck2" name="' . $froomck2 . '" value="1"';
if ($$froomck2) {
    echo " checked='checked'";
}
echo '/>OR ';
makeRoomSelector([$fbuild2, $ffloor2, $froom2], [$build_arr, $floor_arr, $room_arr], 0, $$fbuild2);
makeRoomSelector([$fbuild2, $ffloor2, $froom2], [$build_arr, $floor_arr, $room_arr], 1, $$ffloor2);
makeRoomSelector([$fbuild2, $ffloor2, $froom2], [$build_arr, $floor_arr, $room_arr], 2, $$froom2);
echo "</p>";
echo "プライバシー保護のため、上記で選択された「建物」「フロア」「教室」のうち、もっとも指定範囲が広いレベルで検索出力されます。<br>";
echo "検索結果に教室名まで表示するためには、選択した全てのリスト上で「建物」「フロア」「教室」の全て指定して下さい。<br>";
echo "ボタンビーコンでは、発信種別「S(ingle Click)」「L(ong Click)」「D(ouble Click)」が記録・選択可能です。";
echo "固定・携帯ビーコンの発信種別は「S」のみが利用可能です（「L」「D」の選択も可能ですが、記録情報は存在しません）。";
echo "</div>";

//foreach ($uq_room_arr as $k => $v) {
//    foreach ($v as $k2 => $v2) {
//        echo "$v2\n";
//    }
//}

echo '<div class="item"><h3>送信タイプ</h3><p>';
makeTypeSelector($ftype, $$ftype);
//makeSelector($link, $dbtbl, $ftype, $$ftype);
echo <<< EOF
</p>
<p>M =「手動送信」、A =「自動送信」<br>
</p></div>
EOF;

echo '<div class="item"><h3>日付</h3><p>';
echo '<p><input type="text" id="datefrom" name="' . $date_from . '" value="' . $$date_from . '">';
echo '〜 <input type="text" id="dateto" name="' . $date_to . '" value="' . $$date_to . '">';
//makeDtSelector($link, $dbtbl, $fdt, $date_from, $date_to, $$date_from, $$date_to);
echo <<< EOF
</p><p>
</p></div>
EOF;

echo '<div class="item"><h3>曜日</h3><p>';
makeWdCheckbox($fwd, $warray, $$fwd);
//makeWdSelector($link, $dbtbl, $fwd, $warray, $$fwd);
echo <<< EOF
</p><p>
</p></div>
EOF;

echo '<div class="item"><h3>講時</h3><p>';
makeHpCheckbox($fhour, $parray, $parray_from, $parray_to, $$fhour);
//makeHpSelector($link, $dbtbl, $fhour, $parray, $parray_from, $parray_to, $$fhour);
echo <<< EOF
</p><p>
</p></div>
EOF;

/*****
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
*****/
/*****
echo '<div class="item item-8"><h3>距離</h3><p>';
makeApSelector($link, $dbtbl, $fdist, $apnum, $$fdist);
echo <<< EOF
</p>
<p>M 「手動送信」<br>
A 「自動送信」<br>
</p></div>
EOF;
*****/
echo <<< EOF
<p>
<button type='submit' value='search' class='button'>検索</button>
（1回の検索あたりの検索出力上限は 9999 です）
</p>
</form>
</div>
EOF;

echo '<div class="container"><div class="result">';
//////////////////////////////
if ($_SERVER["REQUEST_METHOD"] == 'POST') {
    $sql = "SELECT * FROM $dbtbl WHERE ";
    //////////////////////////////
    $sql .= "$fid LIKE '" . str_replace('*', '%', $$fid) . "' AND ";
    //////////////////////////////
    if ($$ftype != '*') {
        $sql .= "$ftype LIKE '" . $$ftype . "%' AND ";
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
    $uq_fwd = array_values(array_unique($$fwd));
    if (count($uq_fwd) != 0 && count($uq_fwd) < 7) {
        $sql .= "(";
        foreach ($uq_fwd as $w) {
            $sql .= "dayofweek($fdt)=" . $w . " OR ";
        }
        $sql .= " False) AND ";
    }
    //////////////////////////////
    $uq_fhour = array_values(array_unique($$fhour));
    if (count($uq_fhour) != 0 && count($uq_fhour) < 7) {
        $sql .= "(";
        foreach ($uq_fhour as $h) {
            $sql .= "(time(datetime) between '" . $parray_from[$h] . ":00' AND '" . $parray_to[$h] . ":00') OR ";
        }
        $sql .= " False ) AND ";
    }
    
    /*
    if ($$fhour != 0) {
        $sql .= "(time(datetime) between '" . $parray_from[$$fhour] . ":00' AND '" . $parray_to[$$fhour] . ":00') AND ";
        //$sql .= "( date_format(datetime, '%h:%m:%s') between '" . $parray_from[$$fhour] . ":00' AND '" . $parray_to[$$fhour] . ":00') AND ";
    }*/
    //////////////////////////////
    $place0 = '*';
    $plevel = array( 0 => 0);

    if ($room0 != '' && $room0 != '*') {
        $place0 = $room0;
        $plevel[0] = 3;
    } elseif ($floor0 != '' && $floor0 != '*') {
        $place0 = $floor0;
        $plevel[0] = 2;
    } elseif ($build0 != '' && $build0 != '*') {
        $place0 = $build0;
        $plevel[0] = 1;
    }
    if ($place0 != '*') {
        $sql .= "("; 
        foreach ($place_code[$place0] as $code) {
            for ($i=0; $i<$apnum; $i++) {
                $sql .=  "(" . $fmajor . $i . "='" . $code[0] . "' AND " . $fminor . $i . "='" . $code[1] . "') OR ";
            }
        }
        $sql .= "False) ";
    } else {
        $sql .= " True ";
    }
    /////
    if ($$froomck1) {
        $plevel[1] = 0;
        $place1 = '*';
        if ($room1 != '' && $room1 != '*') {
            $place1 = $room1;
            $plevel[1] = 3;
        } elseif ($floor1 != '' && $floor1 != '*') {
            $place1 = $floor1;
            $plevel[1] = 2;
        } elseif ($build1 != '' && $build1 != '*') {
            $place1 = $build1;
            $plevel[1] = 1;
        }
        $sql .= "OR ("; 
        foreach ($place_code[$place1] as $code) {
            for ($i=0; $i<$apnum; $i++) {
                $sql .=  "(" . $fmajor . $i . "='" . $code[0] . "' AND " . $fminor . $i . "='" . $code[1] . "') OR ";
            }
        }
        $sql .= "False) "; 
    }
    /////
    if ($$froomck2) {
        $plevel[2] = 0;
        $place2 = '*';
        if ($room2 != '' && $room2 != '*') {
            $place2 = $room2;
            $plevel[2] = 3;
        } elseif ($floor2 != '' && $floor2 != '*') {
            $place2 = $floor2;
            $plevel[2] = 2;
        } elseif ($build2 != '' && $build2 != '*') {
            $place2 = $build2;
            $plevel[2] = 1;
        }
        $sql .= "OR ("; 
        foreach ($place_code[$place2] as $code) {
            for ($i=0; $i<$apnum; $i++) {
                $sql .=  "(" . $fmajor . $i . "='" . $code[0] . "' AND " . $fminor . $i . "='" . $code[1] . "') OR ";
            }
        }
        $sql .= "False) "; 
    }
    $sql .= " AND ";

    //////////////////////////////
    $sql .= "True Limit 9999";

    echo "</div>\n";
    //echo "<p>${sql}</p></div>\n";
    echo "<h2>Search Result: ";
    if ($result = $link->query($sql)) {
        echo "$result->num_rows records found.</h2>";
        echo '<p class="box"><a href="' . $download . '">Download CSV file</a></p>';
        echo '<div class="result"><pre>';

        $csv = '';
        $data = array();
        $room_level = min($plevel);
        while ($row = $result->fetch_assoc()) {
            foreach ($row as $key => $value) {
                $data[$key] = $value;
                if ($key == $fid || $key == $fdt) {
                    $csv .= "$value, ";
                } elseif ($key == $ftype) {
                    $csv .= strtoupper(substr($value, 0, 1)) . ", ";
                }
                //echo "$value, ";
            }
	        for ($i=0; $i<$apnum; $i++) {
                if (!empty($build[$data["major$i"]][$data["minor$i"]]) && $room_level > 0) {
                    $csv .= $build[$data["major$i"]][$data["minor$i"]] . '_';
                    //echo $build[$data["major$i"]][$data["minor$i"]] . '_';
                }
                if (!empty($floor[$data["major$i"]][$data["minor$i"]]) && $room_level > 1) {
                    $csv .= $floor[$data["major$i"]][$data["minor$i"]] . '_';
                    //echo $floor[$data["major$i"]][$data["minor$i"]] . '_';
                }
                if (!empty($room[$data["major$i"]][$data["minor$i"]]) && $room_level > 2) {
                    $csv .= $room[$data["major$i"]][$data["minor$i"]];
                    //echo $room[$data["major$i"]][$data["minor$i"]];
                }
                if ($i != $apnum) {
                    $csv .= ", ";
                }
                //echo ", ";
            }
            $csv .= "\n";
            //echo "<br>";
        }
        $_SESSION['csv'] = $csv;
        //echo $_SESSION['csv'];
        echo $csv;
        echo "</pre></div>\n";
    } else {
        echo "</h2>\n<p>Query Error</p><br>\n";
    }
}
$link->close();
echo '</div>';
?>
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
function makeTypeSelector($key, $init) {
    $types = array('*', 'M', 'A');
    echo '<select name="' . $key . '">' . '\n';
    //echo '<option value="*">*</option>' . "\n";
    foreach ($types as $t) {
        echo '<option value="' . $t . '"';
        if ($t == $init) {
            echo ' selected>';
        } else {
            echo '>';
        }
        echo $t . "</option>\n";
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
function makeWdCheckbox($key, $warray, $init = ['1', '2', '3', '4', '5', '6', '7'])
{
    echo '<label><input id="wdCheckAll" type="checkbox" value=""';
    if (count($init) > 6) {
        echo ' checked="checked"';
    }
    echo '>全て</label>';
    for ($i = 1; $i < count($warray); $i++) {
        echo '<label><input name="' . $key . '[]"type="checkbox" value="' . $i;
        if (in_array($i, $init)) {
            echo '" checked="checked';
        }
        echo '">';
        echo $warray[$i] . '</label>';
    }
}
/*
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
}*/
function makeHpCheckbox($key, $parray, $pfrom, $pto, $init = ['0', '1', '2', '3', '4', '5', '6'])
{
    echo '<label><input id="hpCheckAll" type="checkbox" value=""';
    if (count($init) > 6) {
        echo ' checked="checked"';
    }
    echo '>全て</label>';
    for ($i = 0; $i < count($parray); $i++) {
        echo '<label><input name="' . $key . '[]"type="checkbox" value="' . $i;
        if (in_array($i, $init)) {
            echo '" checked="checked';
        }
        echo '">';
        echo $parray[$i] . '(' . $pfrom[$i] . '-' . $pto[$i] . ')</label>';
    }
}
/*
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
}*/
function makeRoomSelector($keyarr, $uq_arr, $level, $init = '*')
{
    echo '<select name="' . $keyarr[$level] . '" id="' . $keyarr[$level] . '"';
    //if ($level != 0 && $init == '*') {
    //    echo ' disabled';
    //}
    echo '>' . "\n";
    echo '<option data-val="*" value="*"';
    if ($init == '*') {
        echo ' selected';
    }
    echo '>*</option>' . "\n";

    foreach ($uq_arr[$level] as $p => $val) {
        if ($level != 0) {
            foreach ($val as $keyarr[$level] => $v) {
                $value = $p . '_' . $v;
                echo '<option data-val="' . $p . '" value="' . $value . '"';
                if ($init == $value) {
                    echo ' selected';
                }
                echo '>' . $v . "</option>\n";
            }
        } else {
            echo '<option value="' . $val . '"';
            if ($init == $val) {
                echo ' selected';
            }
            echo '>' . $val . "</option>\n";
        }
    }
    echo "</select>\n";
}
?>