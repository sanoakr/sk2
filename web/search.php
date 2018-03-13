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
                padding: 2em;
            }
            .content {
                padding: 1em;
            }
            .alert {
                border: 1px solid #000;
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

        $farr = array('id', 'type', 'datetime', 'ssid0', 'bssid0', 'signal0', 'ssid1', 'bssid1', 'signal1', 'ssid2', 'bssid2', 'signal2');

        //////////////////////////////    
        $link = new mysqli($server, $user, $pass, $dbname);
        if ($sql_error = $link->connect_error){
            error_log($sql_error);
            die($sql_error);
        } else {
            $link->set_charset($dbchar);
            //echo "connect and use success!<br>\n";
        }
        $sql = "SELECT COUNT(*) FROM $dbtbl";
        if ($result = $link->query($sql)) {
            $row = $result->fetch_row();
            echo "<p>sk2 has " . $row[0] . " records.</p><br>\n";
        }

        //////////////////////////////
        echo "<div name='form'><form action='search.php' method ='post'>\n";
        foreach($farr as $key) {
            echo "$key ";
            makeSelector($link, $dbtbl, $key);
        }
        unset($key);
        
        echo "<br><p><input type='submit' value='search'></p></div><br>\n";
        if ($_SERVER["REQUEST_METHOD"] == 'POST') {
            echo "<h2>Search Result</h2>\n";
            foreach($farr as $key) {
                $$key = $_POST[$key];
            }
            unset($key);

            foreach($farr as $key) {
                echo " $key=". $$key;
            }
            unset($key);
            echo "<h2></h2>\n";
            $sql = "SELECT * FROM $dbtbl WHERE";
            foreach($farr as $key) {
                if ($$key != '*') {
                    $sql .= " $key='" . $$key . "' AND ";
                }
            }
            $sql .= "True";
            echo "$sql<h2></h2>\n";

            if ($result = $link->query($sql)) {
                while ($row = $result->fetch_assoc()) {
                    echo "<div><pre>\n";
                    foreach( $row as $key => $value ) {
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
    function makeSelector($link, $tbl, $name) {
        $sql = "SELECT DISTINCT $name FROM $tbl";
        echo '<select name="' . $name . '">\n';
        $selector = '<select name="' . $name . '">\n';
        echo '<option value="*">' . $name . ': *</option>\n';
        if ($result = $link->query($sql)) {
            while ($row = $result->fetch_array()) {
                echo '<option value=' . $row[$name] . '>' . $row[$name] . '</option>\n';
            }
        }
        echo "</select>\n";
        return $selector;
    }
?>  