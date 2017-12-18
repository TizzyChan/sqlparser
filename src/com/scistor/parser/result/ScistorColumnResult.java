package com.scistor.parser.result;

import java.util.List;

import com.scistor.parser.column.ScistorColumn;
import com.scistor.parser.column.ScistorSelectColumn;
import com.scistor.parser.exception.ScistorParserException;

public abstract class ScistorColumnResult extends ScistorResult{

	public abstract List<ScistorColumn> getConditionColumns() throws ScistorParserException;
	public abstract void addConditionColumn(ScistorColumn column) throws ScistorParserException;
	public abstract void replaceWherePart(ScistorSelectColumn column) throws ScistorParserException;
	
}
