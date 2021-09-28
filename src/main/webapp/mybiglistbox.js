/**
 * Purpose: 
 * Based on version: 
 */
zk.afterLoad('zkmax.big', function() { //specify zk widget package name
    var exWidget = {};
    zk.override(zkmax.big.Biglistbox.prototype, exWidget, { //specify zk full widget name
        $init: function(e){
            exWidget.$init.apply(this, arguments); //call the original widget's overridden function
            console.log('custom init');
        },
    });

    biglistbox$mold$ = function(out){
        console.log('custom mold');
    }
});
