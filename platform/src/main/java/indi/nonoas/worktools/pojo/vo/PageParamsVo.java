package indi.nonoas.worktools.pojo.vo;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author Nonoas
 * @date 2022/5/16
 */
public class PageParamsVo {
	private final SimpleLongProperty id = new SimpleLongProperty();
	private final SimpleStringProperty paramCode = new SimpleStringProperty();
	private final SimpleStringProperty paramVal = new SimpleStringProperty();

	public long getId() {
		return id.get();
	}

	public SimpleLongProperty idProperty() {
		return id;
	}

	public void setId(long id) {
		this.id.set(id);
	}

	public String getParamCode() {
		return paramCode.get();
	}

	public SimpleStringProperty paramCodeProperty() {
		return paramCode;
	}

	public void setParamCode(String paramCode) {
		this.paramCode.set(paramCode);
	}

	public String getParamVal() {
		return paramVal.get();
	}

	public SimpleStringProperty paramValProperty() {
		return paramVal;
	}

	public void setParamVal(String paramVal) {
		this.paramVal.set(paramVal);
	}
}
