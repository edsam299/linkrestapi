package ukit.model;

import java.io.Serializable;

public class JsonResponse implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6316545729607536207L;
	private boolean value;
	private String descrpition;
	private Object data;
	public JsonResponse() {
//		super();
		// TODO Auto-generated constructor stub
	}
	public JsonResponse(boolean value, String descrpition, Object data) {
//		super();
		this.value = value;
		this.descrpition = descrpition;
		this.data = data;
	}
	public boolean isValue() {
		return value;
	}
	public void setValue(boolean value) {
		this.value = value;
	}
	public String getDescrpition() {
		return descrpition;
	}
	public void setDescrpition(String descrpition) {
		this.descrpition = descrpition;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return "JsonResponse [value=" + value + ", descrpition=" + descrpition + ", data=" + data + "]";
	}

}
