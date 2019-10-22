<?php
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
?>