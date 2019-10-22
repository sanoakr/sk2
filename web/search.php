<?php
$lifetime=3*60; // 3min
session_start();
setcookie(session_name(),session_id(),time()+$lifetime);
?>
<?php require(dirname(__FILE__) . '/search-util.php'); ?>

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
    <script src="js/choice_select.js"></script>
</head>

<?php require(dirname(__FILE__) . '/search-vals.php'); ?>
<?php require(dirname(__FILE__) . '/search-init.php'); ?>
<?php require(dirname(__FILE__) . '/search-jsonroom.php'); ?>

<?php
$msg_id = <<< EOF
<p>
検索パターンのワイルドカードは *、任意の1文字は _ です。<br>
例：「*」 = 「全ユーザー」、「T*」 = 「理工学部生」、「_19*」 = 「全ての学部の2019年度入学生」
</p>
EOF;
$msg_room = <<< EOF
<p>
プライバシー保護のため、上記で選択された「建物」「フロア」「教室」のうち、もっとも指定範囲が広いレベルで検索出力されます。<br>
検索結果に教室名まで表示するためには、選択した全てのリスト上で「建物」「フロア」「教室」の全て指定して下さい。<br>
ボタンビーコンでは、発信種別「S(ingle Click)」「L(ong Click)」「D(ouble Click)」が記録・選択可能です。
固定・携帯ビーコンの発信種別は「S」のみが利用可能です（「L」「D」の選択も可能ですが、記録情報は存在しません）。
</p>
EOF;
$msg_type = <<< EOF
<p>
M =「手動送信」、A =「自動送信」<br>
</p>
EOF;
?>

<?php
//////////////////////////////
echo "<form action='search.php' method ='post'>";
echo '<div class="item"><h3>学籍番号</h3>';
echo '<p><input type="text" name="' . $fid . '" value=' . $$fid . '></p>';
echo $msg_id;
echo '</div>';

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
echo $msg_room;
echo '</div>';

//foreach ($uq_room_arr as $k => $v) {
//    foreach ($v as $k2 => $v2) {
//        echo "$v2\n";
//    }
//}

echo '<div class="item"><h3>送信タイプ</h3><p>';
makeTypeSelector($ftype, $$ftype);
//makeSelector($link, $dbtbl, $ftype, $$ftype);
echo '</p>';
echo $msg_type;
echo '</div>';

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
        $sql .= "( ("; 
        foreach ($place_code[$place0] as $code) {
            for ($i=0; $i<$apnum; $i++) {
                $sql .=  "(" . $fmajor . $i . "='" . $code[0] . "' AND " . $fminor . $i . "='" . $code[1] . "') OR ";
            }
        }
        $sql .= "False) ";
    } else {
        $sql .= " ( True ";
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
    $sql .= " ) AND ";

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
        $_SESSION['adm'] = 'user';
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