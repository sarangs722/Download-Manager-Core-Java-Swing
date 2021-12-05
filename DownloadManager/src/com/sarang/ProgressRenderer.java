package com.sarang;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class ProgressRenderer extends JProgressBar implements TableCellRenderer{
	public ProgressRenderer(int min, int max) {
		super(min, max);
	}
	public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		//Set JProgressBar's percent complete value
		setValue((int)((Float)value).floatValue());
		return this;
	}
}
