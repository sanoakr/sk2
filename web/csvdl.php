<?php
session_start();

header("Content-Type: application/octet-stream");
header("Content-Disposition: attachment; filename=" . date('YmdHis') . rand() . '.csv');
header("Content-Transfer-Encoding: binary");

echo "Id, Type, Datetime, Room1, Room2, Room3,\n";
//echo "Id, Type, Datetime, Major1, Minor1, Dist.1,  Major2, Minor2, Dist.2,  Major3, Minor3, Dist.3, Room1, Room2, Room3,\n";
echo $_SESSION['csv'];
return;
?>