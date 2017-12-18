package com.scistor.parser.authentication;

import com.scistor.parser.result.ScistorResult;

/**
 * 权限认证和加解密扩展接口
 * @author GuoLiang
 */
public interface ScistorAuthenticate {
	
	/**
	 * 权限认证和加解密的接口
	 * 也可以不实现此接口，实现方法就是解析后获取解析结果，再根据结果做权限
	 * @return
	 */
	public int auth(ScistorResult result);
	
}
