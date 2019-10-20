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
    //// 階層的なセレクトボックスで、未選択時のセレクタ階層の有効化・無効化を設定
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
        choice_select(2, [$('#build0'), $('#floor0'), $('#room0')], orgs0);
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
</head>

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