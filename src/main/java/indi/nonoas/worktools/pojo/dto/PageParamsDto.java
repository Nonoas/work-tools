package indi.nonoas.worktools.pojo.dto;

/**
 * 对应表 page_params
 * @author Nonoas
 * @date 2022/1/6
 */
public class PageParamsDto {

	private long id;
	private String paramCode;
	private String paramVal;

	/**
	 * 最后使用的时间戳
	 */
	private long lastUseTimestamp;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getParamCode() {
		return paramCode;
	}

	public void setParamCode(String paramCode) {
		this.paramCode = paramCode;
	}

	public String getParamVal() {
		return paramVal;
	}

	public void setParamVal(String paramVal) {
		this.paramVal = paramVal;
	}

	public long getLastUseTimestamp() {
		return lastUseTimestamp;
	}

	public void setLastUseTimestamp(long lastUseTimestamp) {
		this.lastUseTimestamp = lastUseTimestamp;
	}

	@Override
	public String toString() {
		return this.paramVal;
	}
}
