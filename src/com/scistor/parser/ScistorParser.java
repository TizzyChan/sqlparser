package com.scistor.parser;

import com.scistor.parser.exception.ScistorParserException;
import com.scistor.parser.result.ScistorResult;

/**
 * 解析接口
 * @author GuoLiang
 */
public interface ScistorParser {
	
	/**
	 * 获得解析结果
	 * @return ScistorResult
	 * @throws ScistorParserException
	 */
	public ScistorResult getParseResult() throws ScistorParserException;
	
}
