<?php
$lifetime=10; // 10sec
session_start();
setcookie(session_name(),session_id(),time()+$lifetime);

header("Content-Type: application/octet-stream");
header("Content-Disposition: attachment; filename=" . date('YmdHis') . rand() . '.csv');
header("Content-Transfer-Encoding: binary");

if (strcmp($_SESSION['adm'], 'admin') == 0) {
    echo "Id,Type,Datetime,Major1,Minor1,Dist1,Major2,Minor2,Dist2,Major3,Minor3,Dist3,Room1,Room2,Room3,Unused\n";
} else {
    echo "Id,Type,Datetime,Room1,Room2,Room3,Unused\n";
}

echo $_SESSION['csv'];
return;
?>