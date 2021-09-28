package com.immediando.util.component;

import java.util.LinkedHashSet;
import java.util.Set;

import org.zkoss.lang.Classes;
import org.zkoss.lang.Objects;
import org.zkoss.lang.Strings;
import org.zkoss.xel.VariableResolver;
import org.zkoss.zk.au.AuRequests;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.ext.Scopes;
import org.zkoss.zk.ui.sys.ShadowElementsCtrl;
import org.zkoss.zk.ui.util.ForEachStatus;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zkmax.zul.Biglistbox;
import org.zkoss.zkmax.zul.MatrixComparatorProvider;
import org.zkoss.zkmax.zul.MatrixModel;
import org.zkoss.zkmax.zul.MatrixRenderer;
import org.zkoss.zkmax.zul.event.CellClickEvent;
import org.zkoss.zkmax.zul.event.ScrollEventExt;
import org.zkoss.zkmax.zul.event.SortEventExt;
import org.zkoss.zul.Html;
import org.zkoss.zul.Label;
import org.zkoss.zul.event.ListDataEvent;
import org.zkoss.zul.event.ListDataListener;
import org.zkoss.zul.event.ZulEvents;
import org.zkoss.zul.ext.Selectable;
import org.zkoss.zul.ext.Sortable;
import org.zkoss.zul.impl.Utils;

public class MyBigListbox extends Biglistbox {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private transient MatrixModel<?, ?, ?, ?> _model;

	private transient ListDataListener _dataListener;

	private transient MatrixRenderer<?> _matrixRenderer;

	private transient boolean _childable;

	private transient String[][] _tmpRowData;
	private transient String[][] _tmpColData;

	// for frozen column
	private transient String[][] _tmpRowDataFx;
	private transient String[][] _tmpColDataFx;

	private MatrixComparatorProvider<?> _sortAsc, _sortDsc;

	private int _jsel = -1;

	private int _rows = 30;

	private int _cols = 30;

	private String _rowHeight = "30px";

	private String _colWidth = "60px";

	private int _currentX = 0;

	private int _currentY = 0;

	private int _deltaX = 0;

	private int _deltaY = 0;

	private int _deltaX1 = 0;

	private int _deltaY1 = 0;

	private int _preloadsz = -1;

	private String _scOddRow = null;

	private int _sortingColumnIndex = -1;

	// TODO: support multiple selection
	private boolean _multiple = false;
	private int _frozenCols = 0;
	private boolean _fixFrozenCols = false;
	private boolean _autoCols = true;
	private boolean _autoRows = true;

	private static final int COLUMN = 0x0001;
	private static final int ROW = 0x0002;
	private static final int FROZEN_COLUMN = 0x0010;
	private static final int FROZEN_ROW = 0x0020;
	private static final int ALL = COLUMN | ROW | FROZEN_COLUMN | FROZEN_ROW;

	private static final String ATTR_ON_INIT_RENDER_POSTED = "org.zkoss.zul.onInitLaterPosted";

	static {
		addClientEvent(Biglistbox.class, Events.ON_SELECT, CE_DUPLICATE_IGNORE | CE_IMPORTANT);
		addClientEvent(Biglistbox.class, Events.ON_SCROLL, CE_DUPLICATE_IGNORE);
		addClientEvent(Biglistbox.class, "onScrollY", CE_DUPLICATE_IGNORE | CE_IMPORTANT | CE_NON_DEFERRABLE);
		addClientEvent(Biglistbox.class, "onScrollX", CE_DUPLICATE_IGNORE | CE_IMPORTANT | CE_NON_DEFERRABLE);
		addClientEvent(Biglistbox.class, "onAdjustRows", CE_DUPLICATE_IGNORE | CE_IMPORTANT | CE_NON_DEFERRABLE);
		addClientEvent(Biglistbox.class, "onAdjustCols", CE_DUPLICATE_IGNORE | CE_IMPORTANT | CE_NON_DEFERRABLE);
		addClientEvent(Biglistbox.class, Events.ON_SORT, CE_DUPLICATE_IGNORE);

		addClientEvent(Biglistbox.class, "onCellClick", CE_DUPLICATE_IGNORE);

		addClientEvent(Biglistbox.class, "onAdjustFrozenCols", CE_DUPLICATE_IGNORE | CE_IMPORTANT | CE_NON_DEFERRABLE);
	}

	/**
	 * Sets whether enables auto adjusting the number of cols.
	 * <p>Default: true.
	 */
	public void setAutoCols(boolean autoCols) {
		if (_autoCols != autoCols) {
			_autoCols = autoCols;
			invalidate(); // regenerate the data
		}
	}

	/**
	 * Returns whether enables the auto adjusting cols size.
	 * <p>Default: true.
	 */
	public boolean isAutoCols() {
		return _autoCols;
	}

	/**
	 * Sets whether enables auto adjusting the number of rows.
	 * <p>Default: true.
	 */
	public void setAutoRows(boolean autoRows) {
		if (_autoRows != autoRows) {
			_autoRows = autoRows;
			invalidate(); // regenerate the data
		}
	}

	/**
	 * Returns whether enables the auto adjusting rows size.
	 * <p>Default: true.
	 */
	public boolean isAutoRows() {
		return _autoRows;
	}

	/**
	 * Sets the size of the frozen columns.
	 * @param fcols the size of the frozen columns, it cannot be negative.
	 */
	public void setFrozenCols(int fcols) {
		if (fcols < 0)
			throw new UnsupportedOperationException("No negative value: " + fcols);
		if (_model != null && _model.getColumnSize() < fcols)
			throw new UiException("FrozenCols cannot be greater than model's getColumnSize()");

		if (_frozenCols != fcols) {
			if (fcols < _frozenCols) {
				final int diff = _frozenCols - fcols;
				if (_currentX - diff == fcols)
					_currentX -= diff; // _currentX shouldn't be negative
			} else {
				final int diff = fcols - _frozenCols;
				if (fcols == _currentX + diff)
					_currentX += diff;
			}
			_frozenCols = fcols;
			invalidate();
		}
	}

	/**
	 * Returns the size of the frozen columns.
	 * <p>Default: 0
	 */
	public int getFrozenCols() {
		return _frozenCols;
	}

	/**
	 * Sets to fix the frozen columns, if true, meaning the user cannot change
	 * the size of the frozen columns dynamically.
	 */
	public void setFixFrozenCols(boolean fixFrozenCols) {
		if (_fixFrozenCols != fixFrozenCols) {
			_fixFrozenCols = fixFrozenCols;
			smartUpdate("fixFrozenCols", _fixFrozenCols);
		}
	}

	/**
	 * Returns whether is fix frozen columns, that means user cannot change the
	 * size of the frozen columns dynamically.
	 * <p> Default: false
	 */
	public boolean isFixFrozenCols() {
		return _fixFrozenCols;
	}

	/**
	 * Returns the ascending sorter provider, or null if not available.
	 */
	public MatrixComparatorProvider<?> getSortAscending() {
		return _sortAsc;
	}

	/**
	 * Sets the ascending sorter provider, or null for no sorter for the ascending order.
	 * 
	 * @param sorter
	 *            the comparator provider used to return an sorter of the ascending order.
	 */
	public void setSortAscending(MatrixComparatorProvider<?> sorter) {
		if (!Objects.equals(_sortAsc, sorter)) {
			_sortAsc = sorter;
		}
	}

	/**
	 * Sets the ascending sorter provider with the class name, or null for no
	 * sorter for the ascending order.
	 */
	public void setSortAscending(String clsnm)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (!Strings.isBlank(clsnm))
			setSortAscending(toComparator(clsnm));
	}

	/**
	 * Returns the descending sorter provider, or null if not available.
	 */
	public MatrixComparatorProvider<?> getSortDescending() {
		return _sortDsc;
	}

	/**
	 * Sets the descending sorter provider, or null for no sorter for the descending
	 * order.
	 * @param sorter
	 *            he comparator provider used to return an sorter of the 
	 *            descending order.
	 */
	public void setSortDescending(MatrixComparatorProvider<?> sorter) {
		if (!Objects.equals(_sortDsc, sorter)) {
			_sortDsc = sorter;
		}
	}

	/**
	 * Sets the descending sorter provider with the class name, or null for no sorter for
	 * the descending order.
	 */
	public void setSortDescending(String clsnm)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (!Strings.isBlank(clsnm))
			setSortDescending(toComparator(clsnm));
	}

	@SuppressWarnings("rawtypes")
	private MatrixComparatorProvider<?> toComparator(String clsnm)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (clsnm == null || clsnm.length() == 0)
			return null;

		final Page page = getPage();
		final Class cls = page != null ? page.resolveClass(clsnm) : Classes.forNameByThread(clsnm);
		if (cls == null)
			throw new ClassNotFoundException(clsnm);
		if (!MatrixComparatorProvider.class.isAssignableFrom(cls))
			throw new UiException("MatrixComparatorProvider must be implemented: " + clsnm);
		return (MatrixComparatorProvider<?>) cls.newInstance();
	}

	/**
	 * Returns the style class for the odd rows.
	 * <p>
	 * Default: {@link #getZclass()}-odd.
	 * 
	 */
	public String getOddRowSclass() {
		return _scOddRow == null ? getZclass() + "-odd" : _scOddRow;
	}

	/**
	 * Sets the style class for the odd rows. If the style class doesn't exist,
	 * the striping effect disappears. You can provide different effects by
	 * providing the proper style classes.
	 */
	public void setOddRowSclass(String scls) {
		if (scls != null && scls.length() == 0)
			scls = null;
		if (!Objects.equals(_scOddRow, scls)) {
			_scOddRow = scls;
			smartUpdate("oddRowSclass", scls);
		}
	}

	/**
	 * Returns the number of items to preload when receiving the rendering
	 * request from the client.
	 * <p>Default: 50
	 */
	public int getPreloadSize() {
		if (_preloadsz < 0) {
			int sz = 50;
			if ((sz = Utils.getIntAttribute(this, "org.zkoss.zkmax.zul.biglistbox.preloadSize", sz, true)) < 0)
				throw new UiException("nonnegative is required: " + sz);
			_preloadsz = sz;
		}
		return _preloadsz;
	}

	/**
	 * Returns the rows' size of the viewport.
	 * <p>Default: 30
	 */
	public int getRows() {
		return _rows;
	}

	/**
	 * Sets the rows' size of the viewport.
	 * <p>Default: 30, and it will be adjusted by client engine according with
	 * the browser height.  
	 */
	public void setRows(int rows) {
		if (rows < 0)
			rows = 0;
		if (_rows != rows) {
			_rows = rows;
			invalidate();
		}
	}

	/**
	 * Returns the number of columns within the viewport.
	 * <p>Default: 30
	 */
	public int getCols() {
		return _cols;
	}

	/**
	 * Sets the number of columns within the viewport.
	 * <p>Default: 30, and it will be adjusted by client engine according with
	 * the browser width.  
	 */
	public void setCols(int cols) {
		if (cols < 0)
			cols = 0;
		if (_frozenCols > cols - 1)
			throw new UiException("FrozenCols cannot be greater than cols size");
		if (_cols != cols) {
			_cols = cols;
			invalidate();
		}
	}

	/**
	 * Returns the height of the single row.
	 * <p>Default: 30px
	 */
	public String getRowHeight() {
		return _rowHeight;
	}

	/**
	 * Sets the height of the single row.
	 * <p>Default: 32px
	 */
	public void setRowHeight(String rowHeight) {
		if (Strings.isEmpty(rowHeight))
			rowHeight = "32px";
		if (!_rowHeight.equals(rowHeight)) {
			_rowHeight = rowHeight;
			invalidate();
		}
	}

	/**
	 * Returns the width of the single column
	 * <p>Default: 60px
	 */
	public String getColWidth() {
		return _colWidth;
	}

	/**
	 * Sets the width of the single column.
	 * <p>Default: 60px
	 */
	public void setColWidth(String colWidth) {
		if (Strings.isEmpty(colWidth))
			colWidth = "60px";
		if (!_colWidth.equals(colWidth)) {
			_colWidth = colWidth;
			invalidate();
		}
	}

	/**
	 * Returns the matrix model.
	 */
	public MatrixModel<?, ?, ?, ?> getModel() {
		return _model;
	}

	private void resetInfos() {
		_currentX = 0;
		_currentY = 0;
	}

	/**
	 * Sets the matrix model.
	 * <p>Note: the matrix model must implement {@link Selectable} interface
	 */
	public void setModel(MatrixModel<?, ?, ?, ?> model) {
		if (model != null) {
			if (!(model instanceof Selectable))
				throw new UiException(model.getClass() + " must implement " + Selectable.class);
			if (_frozenCols > model.getColumnSize())
				throw new UiException("FrozenCols cannot be greater than model's getColumnSize()");

			if (_model != model) {
				if (_model != null) {
					_model.removeListDataListener(_dataListener);
					resetInfos();
				}
				_model = model;
				initDataListener();
				postOnInitRender();
			}
		} else if (_model != null) {
			_model.removeListDataListener(_dataListener);
			_model = null;
			invalidate();
		}
	}

	/**
	 * Handles a private event, onInitRender. It is used only for
	 * implementation, and you rarely need to invoke it explicitly.
	 */
	@Override
	public void onInitRender() {
		removeAttribute(ATTR_ON_INIT_RENDER_POSTED);
		syncModel();
		invalidate();
	}

	private void syncModel() {
		syncModel0(ALL);
	}

	private void cleanTempData() {
		_tmpRowDataFx = _tmpColDataFx = _tmpRowData = _tmpColData = null;
	}

	private void syncModel0(int type) {
		if (_model != null) {
			final int maxColSize = _model.getColumnSize();
			final int preloadsz = getPreloadSize();
			final int initCols = _currentX > preloadsz ? _currentX - preloadsz : 0;

			int endCols = _currentX + (_cols - _frozenCols) + preloadsz;

			if (endCols > maxColSize) {
				endCols = maxColSize;
			}

			int maxRowSize = _model.getSize();
			int initRows = _currentY > preloadsz ? _currentY - preloadsz : 0;
			int endRows = _currentY + _rows + preloadsz;

			if (endRows > maxRowSize) {
				endRows = maxRowSize;
			}

			_deltaX = _currentX - initCols;
			_deltaY = _currentY - initRows;
			_deltaX1 = endCols - initCols;
			_deltaY1 = endRows - initRows;
			smartUpdate("_deltaX", _deltaX);
			smartUpdate("_deltaY", _deltaY);
			smartUpdate("_deltaX1", _deltaX1);
			smartUpdate("_deltaY1", _deltaY1);
			smartUpdate("_test", "immediando");
			smartUpdate("test", "immediando");

			if ((type & (ALL | FROZEN_COLUMN)) != 0) {
				_tmpRowDataFx = syncRowData(0, initCols > _frozenCols ? _cols : _frozenCols, initRows, endRows);
				smartUpdate("rowDataFx", new Object[] { 0, _currentY, _tmpRowDataFx });
				_tmpColDataFx = syncColData(0, initCols > _frozenCols ? _cols : _frozenCols);
				smartUpdate("colDataFx", new Object[] { 0, _tmpColDataFx });
			}
			smartUpdate("colDataFx", "test string");

			if ((type & (ALL | COLUMN)) != 0) {
				_tmpColData = syncColData(initCols, endCols);
				smartUpdate("colDasta", new Object[] { _currentX, _tmpColData });
			}

			if ((type & (ALL | ROW)) != 0) {
				_tmpRowData = syncRowData(initCols, endCols, initRows, endRows);
				smartUpdate("rowDacta", new Object[] { _currentX, _currentY, _tmpRowData, "immediando" });
			}

			Events.postEvent(ZulEvents.ON_AFTER_RENDER, this, null);
			// notify the component when all of the data have been rendered.
		}
	}

	private String[][] syncColData(int x1, int x2) {
		final boolean old = _childable;
		int initCols = x1;
		int endCols = x2;
		int colSize = endCols - initCols;
		final int headSize = _model.getHeadSize();
		final String[][] tmpColData = new String[headSize][colSize];

		final MatrixRenderer<Object> renderer = getRealMatrixRenderer();

		// column data
		try {
			_childable = true;
			for (int rowIndex = 0; rowIndex < headSize; rowIndex++) {
				final Object value = _model.getHeadAt(rowIndex);
				for (int col = 0, colIndex = initCols, clen = endCols; colIndex < clen; colIndex++) {
					tmpColData[rowIndex][col++] = renderer.renderHeader(this, value, rowIndex, colIndex);
				}
			}
		} catch (Exception e) {
			throw UiException.Aide.wrap(e);
		} finally {
			// clear possible children created in renderer
			_childable = old;
			getChildren().clear();
		}
		return tmpColData;
	}

	private String[][] syncRowData(int x1, int x2, int y1, int y2) {
		final MatrixRenderer<Object> renderer = getRealMatrixRenderer();
		final boolean old = _childable;
		int initCols = x1;
		int endCols = x2;

		int colSize = endCols - initCols;

		int initRows = y1;
		int endRows = y2;
		int rowSize = endRows - initRows;

		final String[][] tmpRowData = new String[rowSize][colSize];
		// row data
		try {
			_childable = true;
			final Selectable<Object> smodel = getSelectableModel();
			_jsel = -1;
			for (int row = 0, rowIndex = initRows, rlen = endRows; rowIndex < rlen; row++, rowIndex++) {
				final Object value = _model.getElementAt(rowIndex);
				for (int col = 0, colIndex = initCols, clen = endCols; colIndex < clen; colIndex++) {
					if (_jsel < 0 && smodel.isSelected(value))
						_jsel = rowIndex;
					tmpRowData[row][col++] = renderer.renderCell(this, value, rowIndex, colIndex);
				}
			}
		} catch (Exception e) {
			throw UiException.Aide.wrap(e);
		} finally {
			// clear possible children created in renderer
			_childable = old;
			getChildren().clear();
		}
		return tmpRowData;
	}

	/**
	 * Return the real matrix renderer, if {@link #getMatrixRenderer()} is null,
	 * the default implementation is assumed.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> MatrixRenderer<T> getRealMatrixRenderer() {
		final MatrixRenderer renderer = getMatrixRenderer();
		return renderer != null ? renderer : (MatrixRenderer<T>) _defMTRend;
	}

	private void postOnInitRender() {
		if (getAttribute(ATTR_ON_INIT_RENDER_POSTED) == null) {
			setAttribute(ATTR_ON_INIT_RENDER_POSTED, Boolean.TRUE);
			Events.postEvent("onInitRender", this, null);
		}
	}

	private static final MatrixRenderer<Object> _defMTRend = new MatrixRenderer<Object>() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public String renderCell(final Component owner, final Object data, final int rowIndex, final int colIndex) {
			final Biglistbox self = (Biglistbox) owner;
			final Template tm = self.getTemplate("rows");
			if (tm == null)
				return Objects.toString(((MatrixModel) self.getModel()).getCellAt(data, colIndex));
			else {
				final Component[] items = ShadowElementsCtrl
						.filterOutShadows(tm.create(owner, null, new VariableResolver() {
					public Object resolveVariable(String name) {
						if ("each".equals(name)) {
							return data;
						} else if ("forEachStatus".equals(name)) {
							return new ForEachStatus() {

								public ForEachStatus getPrevious() {
									return null;
								}

								public Object getEach() {
									return getCurrent();
								}

								public int getIndex() {
									return colIndex;
								}

								public Integer getBegin() {
									return 0;
								}

								public Integer getEnd() {
									return ((Biglistbox) owner).getModel().getSize();
								}

								public Object getCurrent() {
									return data;
								}

								public boolean isFirst() {
									return getCount() == 1;
								}

								public boolean isLast() {
									return getIndex() + 1 == getEnd();
								}

								public Integer getStep() {
									return null;
								}

								public int getCount() {
									return getIndex() + 1;
								}
							};
						} else if ("matrixInfo".equals(name)) {
							return new Integer[] { colIndex, rowIndex };
						} else {
							return null;
						}
					}
				}, null));
				if (items.length != 1)
					throw new UiException("The model template must have exactly one item, not " + items.length);
				items[0].detach(); // remove from owner
				if (items[0] instanceof Label) {
					return ((Label) items[0]).getValue();
				} else if (items[0] instanceof Html) {
					return ((Html) items[0]).getContent();
				}
				throw new UiException("The model template can only support Label or Html component, not " + items[0]);
			}
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public String renderHeader(final Component owner, final Object data, final int rowIndex, final int colIndex) {
			final Biglistbox self = (Biglistbox) owner;
			final Template tm = self.getTemplate("heads");
			if (tm == null)
				return Objects.toString(((MatrixModel) self.getModel()).getHeaderAt(data, colIndex));
			else {
				final Component[] items = ShadowElementsCtrl
						.filterOutShadows(tm.create(owner, null, new VariableResolver() {
					public Object resolveVariable(String name) {
						if ("each".equals(name)) {
							return data;
						} else if ("forEachStatus".equals(name)) {
							return new ForEachStatus() {

								public ForEachStatus getPrevious() {
									return null;
								}

								public Object getEach() {
									return getCurrent();
								}

								public int getIndex() {
									return colIndex;
								}

								public Integer getBegin() {
									return 0;
								}

								public Integer getEnd() {
									return ((Biglistbox) owner).getModel().getSize();
								}

								public Object getCurrent() {
									return data;
								}

								public boolean isFirst() {
									return getCount() == 1;
								}

								public boolean isLast() {
									return getIndex() + 1 == getEnd();
								}

								public Integer getStep() {
									return null;
								}

								public int getCount() {
									return getIndex() + 1;
								}
							};
						} else if ("matrixInfo".equals(name)) {
							return new Integer[] { colIndex, rowIndex };
						} else {
							return null;
						}
					}
				}, null));
				if (items.length != 1)
					throw new UiException("The model template must have exactly one item, not " + items.length);
				items[0].detach(); // remove from owner
				if (items[0] instanceof Label) {
					return ((Label) items[0]).getValue();
				} else if (items[0] instanceof Html) {
					return ((Html) items[0]).getContent();
				}
				throw new UiException("The model template can only support Label or Html component, not " + items[0]);
			}
		}
	};

	/**
	 * Returns the matrix renderer.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> MatrixRenderer<T> getMatrixRenderer() {
		return (MatrixRenderer) _matrixRenderer;
	}

	/**
	 * Sets the matrix renderer.
	 */
	public void setMatrixRenderer(MatrixRenderer<?> renderer) {
		if (_matrixRenderer != renderer) {
			_matrixRenderer = renderer;
			invalidate();
		}
	}

	/**
	 * Sets the matrix renderer from a class string.
	 * @param clsnm the full package name
	 */
	@SuppressWarnings("rawtypes")
	public void setMatrixRenderer(String clsnm) throws ClassNotFoundException, NoSuchMethodException,
			IllegalAccessException, InstantiationException, java.lang.reflect.InvocationTargetException {
		if (clsnm != null)
			setMatrixRenderer((MatrixRenderer) Classes.newInstanceByThread(clsnm));
	}

	// -- ComponentCtrl --//
	public void invalidate() {
		// post onInitRender to rerender content if not done it before
		if ((_tmpRowData == null || (_frozenCols > 0 && (_tmpRowDataFx == null || _tmpColDataFx == null)))
				&& _model != null && _model.getSize() > 0)
			postOnInitRender();

		org.zkoss.zkex.rt.Runtime.init(this);
		super.invalidate();
	}

	protected boolean isChildable() {
		return _childable;
	}

	private void doSelectionChanged() {
		final Selectable<Object> smodel = getSelectableModel();
		if (smodel.isSelectionEmpty()) {
			if (_jsel >= 0)
				setSelectedIndex(-1);
			return;
		}

		if (_jsel >= 0 && smodel.isSelected(_model.getElementAt(_jsel)))
			return; // nothing changed

		for (int i = 0, sz = _model.getSize(); i < sz; i++) {
			if (smodel.isSelected(_model.getElementAt(i))) {
				setSelectedIndex(i);
				return; // done
			}
		}
		setSelectedIndex(-1); // just in case
	}

	@SuppressWarnings("unchecked")
	private Selectable<Object> getSelectableModel() {
		if (_model instanceof Selectable)
			return (Selectable<Object>) _model;
		return null;
	}

	private void initDataListener() {
		if (_dataListener == null)
			_dataListener = new ListDataListener() {
				public void onChange(ListDataEvent event) {
					switch (event.getType()) {
					case ListDataEvent.SELECTION_CHANGED:
						doSelectionChanged();
						return; // nothing changed so need to rerender
					case ListDataEvent.MULTIPLE_CHANGED:
						return; // nothing to do
					case ListDataEvent.CONTENTS_CHANGED:
						smartUpdate("dirtyModel", true);
						syncModel();
						cleanTempData();
						return;
					case ListDataEvent.INTERVAL_ADDED:
					case ListDataEvent.INTERVAL_REMOVED:
					case ListDataEvent.STRUCTURE_CHANGED:
						int colSize = _model.getColumnSize(), rowSize = _model.getSize();
						if (colSize < _currentX)
							_currentX = colSize - (_cols - _frozenCols);
						if (rowSize < _currentY)
							_currentY = rowSize - _rows;
						smartUpdate("dirtyModel", true);
						break;
					}
					postOnInitRender();
				}
			};
		_model.addListDataListener(_dataListener);
	}

	/**
	 * Returns the index of the selected item (-1 if no one is selected).
	 */
	public int getSelectedIndex() {
		return _jsel;
	}

	/**
	 * Returns the selected object.
	 */
	public Object getSelectedObject() {
		return _model != null && _jsel > -1 ? _model.getElementAt(_jsel) : null;
	}

	/**
	 * Selects the item with the given index.
	 */
	private void setSelectedIndex(int jsel) {
		if (jsel < -1)
			jsel = -1;
		if (jsel != _jsel) {
			_jsel = jsel;
			smartUpdate("selectedIndex", jsel);
		}
	}

	// super//
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void renderProperties(org.zkoss.zk.ui.sys.ContentRenderer renderer) throws java.io.IOException {
		super.renderProperties(renderer);

		org.zkoss.zkex.rt.Runtime.init(this);
		int cacheSize = Utils.getIntAttribute(this, "org.zkoss.zkmax.zul.biglistbox.clientCacheSize", 1, true);
		if (cacheSize != 1) {
			renderer.render("_clientCacheSize", cacheSize);
		}

		if (_frozenCols > 0)
			renderer.render("frozenCols", _frozenCols);

		render(renderer, "fixFrozenCols", _fixFrozenCols);

		renderer.render("selectedIndex", _jsel);
		render(renderer, "oddRowSclass", _scOddRow);

		if (!_autoCols)
			renderer.render("autoCols", _autoCols);
		if (!_autoRows)
			renderer.render("autoRows", _autoRows);

		if (_rows != 30)
			render(renderer, "rows", _rows);
		if (_cols != 30)
			render(renderer, "cols", _cols);
		if (!"60px".equals(_colWidth))
			render(renderer, "colWidth", _colWidth);
		if (!"30px".equals(_rowHeight))
			render(renderer, "rowHeight", _rowHeight);

		// ZK-2178: should give default value if model is null
		render(renderer, "rowSize", _model != null ? _model.getSize() : 0);
		render(renderer, "colSize", _model != null ? _model.getColumnSize() : 0);

		if (_sortDsc != null)
			render(renderer, "sortDescending", true);

		if (_sortAsc != null)
			render(renderer, "sortAscending", true);

		if (_sortingColumnIndex >= 0)
			render(renderer, "_sortingColumnIndex", _sortingColumnIndex);

		Sortable sm = getSortableModel();
		if (sm != null) {
			String direction = "natural";
			if (_sortDsc != null)
				direction = sm.getSortDirection(_sortDsc.getColumnComparator(_sortingColumnIndex));
			if ("natural".equals(direction) && _sortAsc != null)
				direction = sm.getSortDirection(_sortAsc.getColumnComparator(_sortingColumnIndex));
			if (!"natural".equals(direction))
				render(renderer, "sortDirection", direction);
		}

		if (_deltaX > 0)
			render(renderer, "_deltaX", _deltaX);
		if (_deltaY > 0)
			render(renderer, "_deltaY", _deltaY);
		if (_deltaX1 > 0)
			render(renderer, "_deltaX1", _deltaX1);
		if (_deltaY1 > 0)
			render(renderer, "_deltaY1", _deltaY1);

		if (_currentX > 0) {
			render(renderer, "_currentX", _currentX);
		}
		if (_currentY > 0) {
			render(renderer, "_currentY", _currentY);
		}

		if (_tmpRowDataFx != null) {
			render(renderer, "rowDataFx", new Object[] { 0, _currentY, _tmpRowDataFx });
			_tmpRowDataFx = null;
		}
		if (_tmpColDataFx != null) {
			render(renderer, "colDataFx", new Object[] { 0, _tmpColDataFx });
			_tmpColDataFx = null;
		}
		if (_tmpRowData != null) {
			render(renderer, "rowData", new Object[] { _currentX, _currentY, _tmpRowData });
			_tmpRowData = null; // purge the row data
		}
		if (_tmpColData != null) {
			render(renderer, "colData", new Object[] { _currentX, _tmpColData });
			_tmpColData = null; // purge the column data
		}

		render(renderer, "_test", "my test value");
	}

	/**
	 * The default zclass is "z-biglistbox"
	 */
	public String getZclass() {
		return (_zclass != null ? _zclass : "z-biglistbox");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void service(org.zkoss.zk.au.AuRequest request, boolean everError) {
		final String cmd = request.getCommand();
		if (cmd.equals(Events.ON_SELECT)) {
			final Object prevSelected = _jsel >= 0 ? _model.getElementAt(_jsel) : null;
			_jsel = ((Integer) request.getData().get("rowIndex")).intValue();
			final Integer x = ((Integer) request.getData().get("columnIndex")).intValue();
			final Integer index = _jsel;
			final Set<Object> selObjs = new LinkedHashSet<Object>();

			if (index >= 0) {
				if (!_multiple)
					selObjs.clear();
				selObjs.add(_model.getElementAt(index));
			}

			if (_model != null)
				getSelectableModel().setSelection(selObjs);

			if (prevSelected != null) {
				final Set<Object> prevSet = new LinkedHashSet<Object>(1);
				prevSet.add(prevSelected);
				Events.postEvent(new SelectEvent(Events.ON_SELECT, this, null, null, null, selObjs, prevSet, prevSet,
						null, new Integer[] { x, index }, 0));
			} else {
				Events.postEvent(new SelectEvent(Events.ON_SELECT, this, null, null, null, selObjs, null, null, null,
						new Integer[] { x, index }, 0));
			}
		} else if ("onScrollY".equals(cmd)) {
			ScrollEventExt evt = ScrollEventExt.getScrollEventExt(request);
			final boolean reload = AuRequests.getBoolean(request.getData(), "reload");
			_currentY = evt.getY();
			_currentX = evt.getX();
			if (reload) {
				if (_frozenCols > 0 && _currentX > _cols) {
					syncModel0(ROW | FROZEN_COLUMN);
				} else {
					syncModel0(ROW);
				}
				cleanTempData();
			}
			Events.postEvent(evt);
		} else if ("onScrollX".equals(cmd)) {
			ScrollEventExt evt = ScrollEventExt.getScrollEventExt(request);
			final boolean reload = AuRequests.getBoolean(request.getData(), "reload");
			_currentY = evt.getY();
			_currentX = evt.getX();
			if (reload) {
				if (_frozenCols > 0 && _currentX > _cols) {
					syncModel0(ROW | COLUMN | FROZEN_COLUMN);
				} else
					syncModel0(ROW | COLUMN); // excluding frozen

				cleanTempData();
			}
			Events.postEvent(evt);
		} else if (Events.ON_SCROLL.equals(cmd)) {
			ScrollEventExt evt = ScrollEventExt.getScrollEventExt(request);
			Events.postEvent(evt);
		} else if ("onAdjustRows".equals(cmd)) {
			_rows = ((Integer) request.getData().get("")).intValue();
		} else if ("onAdjustCols".equals(cmd)) {
			_cols = ((Integer) request.getData().get("")).intValue();
		} else if (cmd.equals(Events.ON_SORT)) {
			SortEventExt evt = SortEventExt.getSortEventExt(request);
			Events.postEvent(evt);
		} else if ("onCellClick".equals(cmd)) {
			Events.postEvent(CellClickEvent.getCellClickEvent(request));
		} else if ("onAdjustFrozenCols".equals(cmd)) {
			final int frozenCols = ((Integer) request.getData().get("")).intValue();
			if (frozenCols < _frozenCols) {
				int diff = _frozenCols - frozenCols;
				if (_currentX - diff == frozenCols)
					_currentX -= diff;
			} else {
				int diff = frozenCols - _frozenCols;
				if (frozenCols == _currentX + diff)
					_currentX += diff;
			}
			_frozenCols = frozenCols;
		} else
			super.service(request, everError);
	}

	public void onSort(SortEventExt event) {
		int columnIndex = event.getColumnIndex();
		_sortingColumnIndex = columnIndex;
		sort(event.isAscending(), columnIndex);
	}

	/**
	 * Returns the sortable model, if any.
	 */
	@SuppressWarnings("rawtypes")
	public Sortable getSortableModel() {
		if (_model instanceof Sortable)
			return (Sortable) _model;
		return null;
	}

	@SuppressWarnings("unchecked")
	public boolean sort(boolean ascending, int columnIndex) {
		final String dir = getSortableModel()
				.getSortDirection(ascending ? getSortAscending().getColumnComparator(columnIndex)
						: getSortDescending().getColumnComparator(columnIndex));
		if (ascending) {
			if ("ascending".equals(dir))
				return false;
		} else {
			if ("descending".equals(dir))
				return false;
		}
		return doSort(ascending, columnIndex);
	}

	/* package */@SuppressWarnings({ "unchecked", "rawtypes" })
	boolean doSort(boolean ascending, int columnIndex) {
		final MatrixComparatorProvider<?> cmpr = ascending ? _sortAsc : _sortDsc;
		if (cmpr == null)
			return false;

		// comparator might be zscript
		Scopes.beforeInterpret(this);
		try {
			final Sortable model = getSortableModel();
			if (model != null) { // live data
				model.sort(cmpr.getColumnComparator(columnIndex), ascending);
			}
		} finally {
			Scopes.afterInterpret();
		}
		return true;
	}

	// -- Serializable --//
	// NOTE: they must be declared as private
	private synchronized void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		s.defaultWriteObject();

		willSerialize(_model);
		s.writeObject(
				_model instanceof java.io.Serializable || _model instanceof java.io.Externalizable ? _model : null);
		willSerialize(_matrixRenderer);
		s.writeObject(
				_matrixRenderer instanceof java.io.Serializable || _matrixRenderer instanceof java.io.Externalizable
						? _matrixRenderer : null);
	}

	@SuppressWarnings("rawtypes")
	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();

		_model = (MatrixModel) s.readObject();
		didDeserialize(_model);
		_matrixRenderer = (MatrixRenderer) s.readObject();
		didDeserialize(_matrixRenderer);
		if (_model != null) {
			initDataListener();
			onInitRender();
		}
	}

	public void sessionWillPassivate(Page page) {
		super.sessionWillPassivate(page);
		willPassivate(_model);
		willPassivate(_matrixRenderer);
	}

	public void sessionDidActivate(Page page) {
		super.sessionDidActivate(page);
		didActivate(_model);
		didActivate(_matrixRenderer);
	}

	public void onPageAttached(Page newpage, Page oldpage) {
		super.onPageAttached(newpage, oldpage);
		if (_model != null && _dataListener != null) {
			_model.removeListDataListener(_dataListener);
			_model.addListDataListener(_dataListener);
		}
	}

	public void onPageDetached(Page page) {
		super.onPageDetached(page);
		if (_model != null && _dataListener != null) {
			_model.removeListDataListener(_dataListener);
		}
	}
	
}