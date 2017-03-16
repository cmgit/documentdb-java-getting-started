package samples.pojos;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

public class SalesOrder {
	
	private String id;
    private String purchaseOrderNumber;
    private int timeToLive;
    private Date orderDate;
    private Date shippedDate;
    private String accountNumber;
    private BigDecimal subTotal;
    private BigDecimal taxAmount;
    private BigDecimal freight;
    private BigDecimal totalDue;
    private SalesOrderDetail[] items;
    
	
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
	public int getTimeToLive() {
		return timeToLive;
	}
	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}
	public Date getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountNumber == null) ? 0 : accountNumber.hashCode());
		result = prime * result + ((freight == null) ? 0 : freight.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + Arrays.hashCode(items);
		result = prime * result + ((orderDate == null) ? 0 : orderDate.hashCode());
		result = prime * result + ((purchaseOrderNumber == null) ? 0 : purchaseOrderNumber.hashCode());
		result = prime * result + ((shippedDate == null) ? 0 : shippedDate.hashCode());
		result = prime * result + ((subTotal == null) ? 0 : subTotal.hashCode());
		result = prime * result + ((taxAmount == null) ? 0 : taxAmount.hashCode());
		result = prime * result + timeToLive;
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
		if (!(obj instanceof SalesOrder)) {
			return false;
		}
		SalesOrder other = (SalesOrder) obj;
		if (accountNumber == null) {
			if (other.accountNumber != null) {
				return false;
			}
		} else if (!accountNumber.equals(other.accountNumber)) {
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
		if (taxAmount == null) {
			if (other.taxAmount != null) {
				return false;
			}
		} else if (!taxAmount.equals(other.taxAmount)) {
			return false;
		}
		if (timeToLive != other.timeToLive) {
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
	public BigDecimal getTaxAmount() {
		return taxAmount;
	}
	public void setTaxAmount(BigDecimal taxAmount) {
		this.taxAmount = taxAmount;
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
	public SalesOrderDetail[] getItems() {
		return items;
	}
	public void setItems(SalesOrderDetail[] items) {
		this.items = items;
	}

}
