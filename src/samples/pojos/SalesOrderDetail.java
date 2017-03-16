package samples.pojos;

import java.math.BigDecimal;

public class SalesOrderDetail {
	private int orderQty;
	private int productId;
	private BigDecimal unitPrice;
	private BigDecimal lineTotal;
	
	public int getOrderQty() {
		return orderQty;
	}
	public void setOrderQty(int orderQty) {
		this.orderQty = orderQty;
	}
	public int getProductId() {
		return productId;
	}
	public void setProductId(int productId) {
		this.productId = productId;
	}
	public BigDecimal getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}
	public BigDecimal getLineTotal() {
		return lineTotal;
	}
	public void setLineTotal(BigDecimal lineTotal) {
		this.lineTotal = lineTotal;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lineTotal == null) ? 0 : lineTotal.hashCode());
		result = prime * result + orderQty;
		result = prime * result + productId;
		result = prime * result + ((unitPrice == null) ? 0 : unitPrice.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SalesOrderDetail)) {
			return false;
		}
		SalesOrderDetail other = (SalesOrderDetail) obj;
		if (lineTotal == null) {
			if (other.lineTotal != null) {
				return false;
			}
		} else if (!lineTotal.equals(other.lineTotal)) {
			return false;
		}
		if (orderQty != other.orderQty) {
			return false;
		}
		if (productId != other.productId) {
			return false;
		}
		if (unitPrice == null) {
			if (other.unitPrice != null) {
				return false;
			}
		} else if (!unitPrice.equals(other.unitPrice)) {
			return false;
		}
		return true;
	}

}
