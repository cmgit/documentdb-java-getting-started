package samples.pojos;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

public class SalesOrder2 {
	
	
	private String id;
	private String purchaseOrderNumber;
	private Date orderDate;
	private Date dueDate;
	private Date shippedDate;
	private String accountNumber;
	private BigDecimal subTotal;
	private BigDecimal taxAmt;
	private BigDecimal freight;
	private BigDecimal totalDue;
	private BigDecimal discountAmt;
	private SalesOrderDetail2[] items;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPurchaseOrderNumber() {
		return purchaseOrderNumber;
	}
	public void setPurchaseOrderNumber(String purchaseOrderNumber) {
		this.purchaseOrderNumber = purchaseOrderNumber;
	}
	public Date getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}
	public Date getDueDate() {
		return dueDate;
	}
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	public Date getShippedDate() {
		return shippedDate;
	}
	public void setShippedDate(Date shippedDate) {
		this.shippedDate = shippedDate;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public BigDecimal getSubTotal() {
		return subTotal;
	}
	public void setSubTotal(BigDecimal subTotal) {
		this.subTotal = subTotal;
	}
	public BigDecimal getTaxAmt() {
		return taxAmt;
	}
	public void setTaxAmt(BigDecimal taxAmt) {
		this.taxAmt = taxAmt;
	}
	public BigDecimal getFreight() {
		return freight;
	}
	public void setFreight(BigDecimal freight) {
		this.freight = freight;
	}
	public BigDecimal getTotalDue() {
		return totalDue;
	}
	public void setTotalDue(BigDecimal totalDue) {
		this.totalDue = totalDue;
	}
	public BigDecimal getDiscountAmt() {
		return discountAmt;
	}
	public void setDiscountAmt(BigDecimal discountAmt) {
		this.discountAmt = discountAmt;
	}
	public SalesOrderDetail2[] getItems() {
		return items;
	}
	public void setItems(SalesOrderDetail2[] items) {
		this.items = items;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountNumber == null) ? 0 : accountNumber.hashCode());
		result = prime * result + ((discountAmt == null) ? 0 : discountAmt.hashCode());
		result = prime * result + ((dueDate == null) ? 0 : dueDate.hashCode());
		result = prime * result + ((freight == null) ? 0 : freight.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + Arrays.hashCode(items);
		result = prime * result + ((orderDate == null) ? 0 : orderDate.hashCode());
		result = prime * result + ((purchaseOrderNumber == null) ? 0 : purchaseOrderNumber.hashCode());
		result = prime * result + ((shippedDate == null) ? 0 : shippedDate.hashCode());
		result = prime * result + ((subTotal == null) ? 0 : subTotal.hashCode());
		result = prime * result + ((taxAmt == null) ? 0 : taxAmt.hashCode());
		result = prime * result + ((totalDue == null) ? 0 : totalDue.hashCode());
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
		if (!(obj instanceof SalesOrder2)) {
			return false;
		}
		SalesOrder2 other = (SalesOrder2) obj;
		if (accountNumber == null) {
			if (other.accountNumber != null) {
				return false;
			}
		} else if (!accountNumber.equals(other.accountNumber)) {
			return false;
		}
		if (discountAmt == null) {
			if (other.discountAmt != null) {
				return false;
			}
		} else if (!discountAmt.equals(other.discountAmt)) {
			return false;
		}
		if (dueDate == null) {
			if (other.dueDate != null) {
				return false;
			}
		} else if (!dueDate.equals(other.dueDate)) {
			return false;
		}
		if (freight == null) {
			if (other.freight != null) {
				return false;
			}
		} else if (!freight.equals(other.freight)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (!Arrays.equals(items, other.items)) {
			return false;
		}
		if (orderDate == null) {
			if (other.orderDate != null) {
				return false;
			}
		} else if (!orderDate.equals(other.orderDate)) {
			return false;
		}
		if (purchaseOrderNumber == null) {
			if (other.purchaseOrderNumber != null) {
				return false;
			}
		} else if (!purchaseOrderNumber.equals(other.purchaseOrderNumber)) {
			return false;
		}
		if (shippedDate == null) {
			if (other.shippedDate != null) {
				return false;
			}
		} else if (!shippedDate.equals(other.shippedDate)) {
			return false;
		}
		if (subTotal == null) {
			if (other.subTotal != null) {
				return false;
			}
		} else if (!subTotal.equals(other.subTotal)) {
			return false;
		}
		if (taxAmt == null) {
			if (other.taxAmt != null) {
				return false;
			}
		} else if (!taxAmt.equals(other.taxAmt)) {
			return false;
		}
		if (totalDue == null) {
			if (other.totalDue != null) {
				return false;
			}
		} else if (!totalDue.equals(other.totalDue)) {
			return false;
		}
		return true;
	}

}
