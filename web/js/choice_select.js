//// 階層的なセレクトボックスで、未選択時のセレクタ階層の有効化・無効化を設定
function choice_select(level, arr, orgs) {
    var parent = arr[level - 1];
    var target = arr[level];
    var gchild = null;
    var pval = parent.val();

    target.html(orgs[level]).find('option').each(function () {
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
$(function () {
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
    $('#build0').change(function () {
        choice_select(1, [$('#build0'), $('#floor0'), $('#room0')], orgs0);
        choice_select(2, [$('#build0'), $('#floor0'), $('#room0')], orgs0);
    });
    $('#floor0').change(function () {
        choice_select(2, [$('#build0'), $('#floor0'), $('#room0')], orgs0);
    });
    $('#build1').change(function () {
        choice_select(1, [$('#build1'), $('#floor1'), $('#room1')], orgs1);
        choice_select(2, [$('#build1'), $('#floor1'), $('#room1')], orgs1);
    });
    $('#floor1').change(function () {
        choice_select(2, [$('#build1'), $('#floor1'), $('#room1')], orgs1);
    });
    $('#build2').change(function () {
        choice_select(1, [$('#build2'), $('#floor2'), $('#room2')], orgs2);
        choice_select(2, [$('#build2'), $('#floor2'), $('#room2')], orgs2);
    });
    $('#floor2').change(function () {
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
    $('#roomCheck1').change(function () {
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
    $('#roomCheck2').change(function () {
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

    $(checkAll).on('click', function () {
        $(checkBox).prop('checked', $(this).is(':checked'));
    });

    $(checkBox).on('click', function () {
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

    $(checkAllH).on('click', function () {
        $(checkBoxH).prop('checked', $(this).is(':checked'));
    });

    $(checkBoxH).on('click', function () {
        var boxCount = $(checkBoxH).length;
        var checked = $(checkBoxH + ':checked').length;
        if (checked === boxCount) {
            $(checkAllH).prop('checked', true);
        } else {
            $(checkAllH).prop('checked', false);
        }
    });
});