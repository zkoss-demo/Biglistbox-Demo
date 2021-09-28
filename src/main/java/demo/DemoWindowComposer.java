package demo;

import com.immediando.util.component.MyBigListbox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.*;
import org.zkoss.zkmax.zul.*;
import org.zkoss.zul.Window;

import java.util.*;

/**
 * A demo of Big listbox to handle 1 trillion data.
 * @author jumperchen
 */
public class DemoWindowComposer extends SelectorComposer<Window> {

	@Wire
	private MyBigListbox myComp;


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

			@Override
			public String renderCell(Component owner, List<String> data,
					int rowIndex, int colIndex) throws Exception {
				String d = data.get(colIndex);
				d = d.replace("ZK", "<span class='red' title='ZK'>ZK</span>")
						.replace("Hello", "<span class='blue' title='Hello'>Hello</span>");
				return "<div class='images_" + (colIndex%28) + "' title='x=" + 
				colIndex + ",y=" + rowIndex + "'>" + d + "</div>";
			}

			@Override
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


	@Listen("onSelect=#myComp")
	public void onSelect(SelectEvent evt) {
		System.out.println("You listen onSelect: "
				+ Arrays.asList(((Integer[]) evt.getData())));
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

		@Override
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

			@Override
			public int compare(List<String> o1, List<String> o2) {
				return o1.get(_mmc._x).compareTo(o2.get(_mmc._x))
						* (_acs ? 1 : -1);
			}
		}
	}
}