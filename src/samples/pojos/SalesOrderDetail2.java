package samples.pojos;

import java.math.BigDecimal;

public class SalesOrderDetail2 {

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currencyCode == null) ? 0 : currencyCode.hashCode());
		result = prime * result + ((currencySymbol == null) ? 0 : currencySymbol.hashCode());
		result = prime * result + ((lineTotal == null) ? 0 : lineTotal.hashCode());
		result = prime * result + orderQty;
		result = prime * result + ((productCode == null) ? 0 : productCode.hashCode());
		result = prime * result + ((productName == null) ? 0 : productName.hashCode());
		result = prime * result + ((unitPrice == null) ? 0 : unitPrice.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SalesOrderDetail2)) {
			return false;
		}
		SalesOrderDetail2 other = (SalesOrderDetail2) obj;
		if (currencyCode == null) {
			if (other.currencyCode != null) {
				return false;
			}
		} else if (!currencyCode.equals(other.currencyCode)) {
			return false;
		}
		if (currencySymbol == null) {
			if (other.currencySymbol != null) {
				return false;
			}
		} else if (!currencySymbol.equals(other.currencySymbol)) {
			return false;
		}
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
		if (productCode == null) {
			if (other.productCode != null) {
				return false;
			}
		} else if (!productCode.equals(other.productCode)) {
			return false;
		}
		if (productName == null) {
			if (other.productName != null) {
				return false;
			}
		} else if (!productName.equals(other.productName)) {
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
	private int orderQty;
	private String productCode;
	private String productName;
	private String currencySymbol;
	private String currencyCode;
	private BigDecimal unitPrice;
	private BigDecimal lineTotal;
	
	public int getOrderQty() {
		return orderQty;
	}
	public void setOrderQty(int orderQty) {
		this.orderQty = orderQty;
	}
	public String getProductCode() {
		return productCode;
	}
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getCurrencySymbol() {
		return currencySymbol;
	}
	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
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
	
}
