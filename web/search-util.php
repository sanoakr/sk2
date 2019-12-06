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
function makeHpCheckbox($key, $parray, $pfrom, $pto, $init = ['0','1','2','3','4','5','6','7','8','9','10','11'])
{
    echo '<label><input id="hpCheckAll" type="checkbox" value=""';
    if (count($init) >= count($parray)) {
        echo ' checked="checked"';
    }
    echo '>全て</label>';
    echo count($init) . ' ';
    echo count($parray) . ' ';
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