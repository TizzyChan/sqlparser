package com.scistor.parser.oracle;

import java.util.List;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.scistor.parser.result.ScistorDropResult;
import com.scistor.parser.result.ScistorResult;
import com.scistor.parser.result.ScistorSQLType;

public class ScistorOracleDropParser  extends ScistorOracleParser{
	public ScistorOracleDropParser(SQLStatement statement) {
		super(statement);
		this.result = new ScistorDropResult();
		this.result.setSqlType(ScistorSQLType.DROP);
	}

	@Override
	public ScistorResult getParseResult() {
		SQLDropTableStatement drop = (SQLDropTableStatement)this.statement;
		List<SQLExprTableSource> tables = drop.getTableSources();
		for(SQLExprTableSource table : tables){
			((ScistorDropResult)this.result).addTableName(table.getExpr().toString());
		}
		return this.result;
	}

}
