package com.immediando;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.lang.Objects;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkmax.zul.Biglistbox;
import org.zkoss.zkmax.zul.MatrixComparatorProvider;
import org.zkoss.zkmax.zul.MatrixModel;
import org.zkoss.zkmax.zul.MatrixRenderer;
import org.zkoss.zul.AbstractListModel;
import org.zkoss.zul.Div;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.ListDataEvent;
import org.zkoss.zul.ext.Sortable;

public class Test1 extends SelectorComposer<Window> {
	@Wire
	private Biglistbox myComp;

	@Wire
	private Div tip;

	@Wire
	private Textbox content;
	
	// images marks
	private String[] images = { "aim", "amazon", "android", "apple", "bebo",
			"bing", "blogger", "delicious", "digg", "facebook", "flickr",
			"friendfeed", "google", "linkedin", "netvibes", "newsvine",
			"reggit", "rss", "sharethis", "stumbleupon", "technorati",
			"twitter", "utorrent", "vimeo", "vkontakte", "wikipedia", "windows",
			"yahoo" };

	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);

		// specify a trillion faker model
		myComp.setModel(new FakerMatrixModel(1000 * 1000, 1000 * 1000));
		myComp.setColWidth("130px");
		myComp.setMatrixRenderer(new MatrixRenderer<List<String>>() {

			public String renderCell(Component owner, List<String> data,
					int rowIndex, int colIndex) throws Exception {
				String d = data.get(colIndex);
				d = d.replace("ZK", "<span class='red' title='ZK'>ZK</span>")
						.replace("Hello", "<span class='blue' title='Hello'>Hello</span>");
				return "<div class='images_" + (colIndex%28) + "' title='x=" + 
				colIndex + ",y=" + rowIndex + "'>" + d + "</div>";
			}

			public String renderHeader(Component owner, List<String> data,
					int rowIndex, int colIndex) throws Exception {
				return "<div class='images_" + (colIndex % 28) + "' title='"
						+ images[colIndex % 28] + "'>" + data.get(colIndex)
						+ "</div>";
			}
		});
		myComp.setSortAscending(new MyMatrixComparatorProvider<List<String>>(true));
		myComp.setSortDescending(new MyMatrixComparatorProvider<List<String>>(false));
	}
	
	// Matrix comparator provider 
	private class MyMatrixComparatorProvider<T> implements
			MatrixComparatorProvider<List<String>> {
		private int _x = -1;

		private boolean _acs;

		private MyComparator _cmpr;

		public MyMatrixComparatorProvider(boolean asc) {
			_acs = asc;
			_cmpr = new MyComparator(this);
		}

		public Comparator<List<String>> getColumnComparator(int columnIndex) {
			this._x = columnIndex;
			return _cmpr;

		}

		// a real String comparator
		private class MyComparator implements Comparator<List<String>> {
			private MyMatrixComparatorProvider _mmc;

			public MyComparator(MyMatrixComparatorProvider mmc) {
				_mmc = mmc;
			}

			public int compare(List<String> o1, List<String> o2) {
				return o1.get(_mmc._x).compareTo(o2.get(_mmc._x))
						* (_acs ? 1 : -1);
			}
		}
	}
}

class FakerMatrixModel<Head extends List, Row extends List, Cell, Header> extends
		AbstractListModel<Row> implements MatrixModel<Row, Head, Cell, Header>, Sortable {
	
	// a rendering function
	private interface Fun<T> {
		public T apply(int index);
	}
	
	// A faker of key list implementation that contains a key to speed up the performance.
	// Because Java Collection framework didn't handle it well for huge data, it will
	// go through whole the list entries to receive the value for those methods,
	// hashCode(), equals(), and toString()
	private class FakerKeyList<T> extends AbstractList<T> {
		final int _size;
		Map<String, T> _updateCache = new HashMap<String,T> ();
		final Fun<?> _fn;
		final String _key;

		public FakerKeyList(int size, int key, Fun<?> fn) {
			_size = size;
			_key = key + "_" + size;
			_fn = fn;
		}

		
		public int size() {
			return _size;
		}

		
		public boolean isEmpty() {
			return _size == 0;
		}

		
		public T get(int index) {
			// if changed, returns the changed value
			Object val = _updateCache.get(String.valueOf(index));
			if (val != null)
				return (T) val;
			return (T) _fn.apply(index);
		}

		
		public T set(int index, T element) {
			_updateCache.put(String.valueOf(index), element);
			return element;
		}

		
		public int hashCode() {
			return _key.hashCode();
		}
		
		
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj instanceof FakerKeyList) {
				return _key.equals(((FakerKeyList)(obj))._key);
			}
			return false;
		}
		
		
		public String toString() {
			return _key;
		}
	}

	private int _colSize;

	private int _rowSize;

	private Map<String, List<String>> _rowCache;

	private List<List<String>> _headerData;

	private Comparator<Cell> _sorting;

	private boolean _sortDir = true;

	@SuppressWarnings("unchecked")
	public void sort(Comparator cmpr, boolean ascending) {
		_sorting = cmpr;
		_sortDir = ascending;
		fireEvent(ListDataEvent.STRUCTURE_CHANGED, -1, -1);
	}

	
	public String getSortDirection(Comparator cmpr) {
		if (Objects.equals(_sorting, cmpr))
			return _sortDir ? "ascending" : "descending";
		return "natural";
	}

	public FakerMatrixModel(int colSize, int rowSize) {
		_colSize = colSize;
		_rowSize = rowSize;
		_rowCache = new HashMap<String, List<String>>();
		_headerData = new ArrayList<List<String>>();
		_headerData.add(new FakerKeyList<String>(colSize, 0, new Fun() {
			
			public Object apply(int index) {
				return "Header x = " + index;
			}
		}));
	}


	public FakerMatrixModel(int colSize, int rowSize, int headSize) {
		_colSize = colSize;
		_rowSize = rowSize;
		_rowCache = new HashMap<String, List<String>>();
		_headerData = new ArrayList<List<String>>();
		for (int i = 0; i < headSize; i++) {
			final int rowIndex = i;
			_headerData.add(new FakerKeyList<String>(colSize, 0, new Fun() {
				
				public Object apply(int index) {
					return "Header x = " + index + ", y = " + rowIndex;
				}
			}));
		}
	}
	public void update(Integer[] axis, String value) {
		List<String> list = _rowCache.get(String.valueOf(axis[1]));
		list.set(axis[0], value);
		this.fireEvent(ListDataEvent.CONTENTS_CHANGED, axis[0], axis[1]);
	}

	public void setSize(int colSize, int rowSize) {
		_colSize = colSize;
		_rowSize = rowSize;
		this.fireEvent(ListDataEvent.STRUCTURE_CHANGED, -1, -1);
	}

	@SuppressWarnings("unchecked")
	public Row getElementAt(int index) {
		final int rowIndex = _sortDir ? index : getSize() - index - 1; // handle the sorting
		final String key = String.valueOf(rowIndex);
		List<String> value = _rowCache.get(key);
		if (value == null) {
			value = new FakerKeyList<String>(_colSize, rowIndex, new Fun() {
				
				public Object apply(int index) {
					return "y = " + rowIndex;
				}});
			_rowCache.put(key, value);
		}
		return (Row) value;
	}

	
	public int getSize() {
		return _rowSize;
	}

	
	public int getColumnSize() {
		return _colSize;
	}

	
	public int getHeadSize() {
		return _headerData.size();
	}

	@SuppressWarnings("unchecked")
	public Head getHeadAt(int rowIndex) {
		return (Head) _headerData.get(rowIndex);
	}

	@SuppressWarnings("unchecked")
	public Cell getCellAt(Row rowData, int columnIndex) {
		return (Cell) rowData.get(columnIndex);
	}

	@SuppressWarnings("unchecked")
	public Header getHeaderAt(Head headData, int columnIndex) {
		return (Header) headData.get(columnIndex);
	}

}
