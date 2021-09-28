zk.afterLoad('zkmax.big', function() { //specify zk widget package name
   	var exWidget = {};
	zk.override(zkmax.big.Biglistbox.prototype, exWidget, { //specify zk full widget name
		_test: 'e',
        setTest: function (data) {
            this._test = data[0];
            console.log(this._test);
        },
        $init: function(e){
            exWidget.$init.apply(this, arguments); //call the original widget's overridden function
            console.log('custom init');
        },
    	_renderRows: function(out, currentX, currentY){
//			console.log(this._test);
			// skip the overdue data
			if (this._lastY == this._currentY) {
				currentY = this._currentY;
			}
			// fixed for ZK-2219: Header getting sometimes out of synch with cells in Biglistbox
			if (this._lastX == this._currentX) {
				currentX = this._currentX;
			}
			
			var uuid = this.uuid,
				rowData = this._rowData;
			
			currentX = currentX != undefined ? currentX : this._currentX;
			currentY = currentY != undefined ? currentY : this._currentY;
	
			this._lastY = currentY;
			
			var cols = this._getVisibleCols(currentX),
				rows = this._getVisibleRows(currentY);
			
			out = out || new zk.Buffer();
			
			out.push('<tbody id="pivello-rows">');
			var scOdd = this.getOddRowSclass();
			for (var r = currentY, even = !!(r % 2), ridx = 0; ridx < rows; ridx++, r++) {
				out.push('<tr style="height:', this._rowHeight, '" class="', this.$s('row'));
				if (this._selectedIndex === r || scOdd) {
					if (scOdd && !even)
						out.push(' ', scOdd);
					if (this._selectedIndex === r)
						out.push(' ', this.$s('selected'));
					even = !even;
				}
				out.push('">');
				for (var c = currentX, cidx = 0; cidx < cols; cidx++, c++)
					out.push('<td data-axis="', c, ',', r, '">', rowData[r][c], '</td>');
				out.push('</tr>');
			}
			out.push('</tbody>');
			return out;
        },
 		_rerenderRows: function(){
			exWidget._rerenderRows.apply(this, arguments); //call the original widget's overridden function
            //implement your custom logic
//			console.log('ciao1');

			//return result;
        },
 		setColDataFx: function (data) {
 		    console.log('caldatafx')
			console.log(data);
			if(data[1] != null) {
				console.log('cb');
				var currentX = data[0],
					colData = data[1],
					x1 = this._frozenCols;
				
				if (!this._colData || this._rects.length > this._clientCacheSize) {
					this._colData = [];
					this._lastX = currentX; //reset
				}
				for (var r = 0, rlen = colData.length; r < rlen; r++) {
					if (!this._colData[r])
						this._colData[r] = [];
					for (var c = 0, c1 = 0; c < x1; c++, c1++)
						this._colData[r][c1] = colData[r][c];
				}
			} else {
				console.log('cc');
				this._test = data[0];
			}
		},

    });

});