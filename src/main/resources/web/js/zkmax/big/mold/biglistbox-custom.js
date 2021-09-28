biglistboxCustom$mold$ =

function (out) {
	var uuid = this.uuid,
		colData = this._colData || [],
		rowData = this._rowData || [],
		fcols = this._frozenCols,
		colWidth = zk.parseInt(this._colWidth),
		tag = zk.ie < 11 ? 'a' : 'button',
		cols = this._getVisibleCols();
	out.push('<div ', this.domAttrs_(), '><div class="', this.$s('outer'), '">');

	if (colData.length) {
		out.push('<div id="', uuid, '-head" class="', this.$s('head-outer'), '" role="row">');
		var headcls = this.$s('head');
		if (fcols) {
			// head-frozen
			out.push('<div id="', uuid, '-headfx" class="', headcls, ' ',
					this.$s('head-frozen'), '" style="width:', fcols * colWidth,
					'px"><table width="100%" style="table-layout:fixed;width:',
					fcols * colWidth , 'px" role="none">');
			this._renderFrozenFaker(out, 'hdfkfx');
			this._renderFrozenCols(out)
				.push('</table></div><div id="', uuid, '-headshim" class="',
						this.$s('head-shim'), '"></div>');
		}

		// head
		out.push('<div id="', uuid, '-head-cnt" class="', headcls, '">',
				'<table width="100%" style="table-layout:fixed;width:',
				cols * colWidth , 'px" role="none">');
		// faker
		this._renderFaker(out, 'hdfk');
		// cols
		this._renderCols(out).push('</table></div>');

		out.push('<div class="z-clear"></div></div>');
	}
	out.push('<div id="', uuid, '-body" class="', this.$s('body-outer'), '" role="rowgroup">');
	var bodycls = this.$s('body');
	if (fcols) {
		// body-frozen
		out.push('<div id="', uuid, '-bodyfx" class="', bodycls, ' ',
				this.$s('body-frozen'), '" style="width:', fcols * colWidth,
				'px"><table width="100%" style="table-layout:fixed;width:',
				fcols * colWidth , 'px">');

		this._renderFrozenFaker(out, 'bdfkfx');
		this._renderFrozenRows(out)
			.push('</table></div><div id="', uuid, '-bodyshim" class="',
					this.$s('body-shim'), '"></div>');
	}
	if (rowData.length) {
		// body
		out.push('<div id="', uuid, '-body-cnt" class="', bodycls, '">',
				'<table width="100%" style="table-layout:fixed;width:',
				cols * colWidth , 'px" role="none">');

		// faker
		this._renderFaker(out, 'bdfk');

		// rows
		this._renderRows(out).push('</table></div>');
	}
	out.push('<div class="z-clear"></div></div>');
	out.push('<', tag, ' id="', uuid, '-a" style="top:0px;left:0px"',
			' onclick="return false;" href="javascript:;" class="z-focus-a"></',
			tag, '></div>');

	if (fcols) {
		out.push('<div id="', uuid, '-vbarfx" style="left:', fcols * colWidth ,
				'px" class="', this.$s('verticalbar-frozen'), '"></div>');
		out.push('<div id="', uuid, '-vbartick" class="', this.$s('verticalbar-tick'), '"></div>');
	}
	out.push('</div>');
	console.log('custom mold');
}