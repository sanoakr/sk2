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
$parray =      array('1講時', '1-2休み', '2講時', '昼休み',  '3講時', '3-4休み', '4講時',  '4-5休み', '5講時', '4-5休み', '6講時');
$parray_from = array('09:20', '10:51',  '11:05', '12:36', '13:35', '15:04',  '15:20', '16:51',  '17:00', '18:31',  '18:40');
$parray_to =   array('10:50', '11:04',  '12:35', '13:34', '15:05', '15:19',  '16:50', '16:59',  '18:30', '18:39',  '20:10');
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