package com.scistor.parser.oracle;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.scistor.parser.oracle.ScistorOracleAlterParser;
import com.scistor.parser.oracle.ScistorOracleCreateParser;
import com.scistor.parser.oracle.ScistorOracleDeleteParser;
import com.scistor.parser.oracle.ScistorOracleDropParser;
import com.scistor.parser.oracle.ScistorOracleInsertParser;
import com.scistor.parser.oracle.ScistorOracleParser;
import com.scistor.parser.oracle.ScistorOracleSelectParser;
import com.scistor.parser.oracle.ScistorOracleUpdateParser;
import com.scistor.parser.oracle.ScistorOtherParser;
import com.scistor.parser.factory.ScistorParserFactory;

public class ScistorOracleParserFactory implements ScistorParserFactory{


		@Override
		public ScistorOracleParser createSelectParser(SQLStatement statement) {
			return new ScistorOracleSelectParser(statement);
		}

		@Override
		public ScistorOracleParser createInsertParser(SQLStatement statement) {
			return new ScistorOracleInsertParser(statement);
		}

		@Override
		public ScistorOracleParser createUpdateParser(SQLStatement statement) {
			return new ScistorOracleUpdateParser(statement);
		}

		@Override
		public ScistorOracleParser createDeleteParser(SQLStatement statement) {
			return new ScistorOracleDeleteParser(statement);
		}

		@Override
		public ScistorOracleParser createAlterParser(SQLStatement statement) {
			return new ScistorOracleAlterParser(statement);
		}

		@Override
		public ScistorOracleParser createCreateParser(SQLStatement statement) {
			
			return new ScistorOracleCreateParser(statement);
		}

		@Override
		public ScistorOracleParser createDropParser(SQLStatement statement) {
			return new ScistorOracleDropParser(statement);
		}

		@Override
		public ScistorOracleParser createOtherParser(SQLStatement statement) {
			return new ScistorOtherParser(statement);
		}

}
