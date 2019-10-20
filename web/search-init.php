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
    $$fhour = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10'];
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